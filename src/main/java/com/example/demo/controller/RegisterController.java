package com.example.demo.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dao.RegionCodeDAO;
import com.example.demo.entity.AuthType;
import com.example.demo.entity.RegionCode;
import com.example.demo.entity.Users;

import com.example.demo.service.RegionCodeService;
import com.example.demo.service.UsersService;
import org.springframework.core.io.Resource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class RegisterController {

    @Autowired
    private RegionCodeService regionCodeService;
	
    @Autowired
	private UsersService userService;
	
	@Autowired
	private RegionCodeDAO rd;
	
	@Value("${kakao.login.pass}")
    private String kakaoGeneralPassword;
	
	//비밀번호 재설정용
	@Autowired
    PasswordEncoder passwordEncoder; 
	
	
    @GetMapping("/register_success")
    public String registerSuccessPage() {
        return "register_success";
    }

    @GetMapping("/register_kakao")
    public String registerFormKakao(@RequestParam("email") String email, @RequestParam("nickname") String nickname, Model model, HttpSession session) {
    	String decodedNickname ="";
    	try {
			decodedNickname = URLDecoder.decode(nickname, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("카카오 닉네임 콘트롤러 전송:"+decodedNickname);
		System.out.println("카카오 이메일 콘트롤러 전송:"+email);

    	model.addAttribute("email", email);
    	model.addAttribute("nickname", decodedNickname);
    	model.addAttribute("regionCodes", rd.findAll()); // Assuming regions are needed for the form

        model.addAttribute("kakaopassword", kakaoGeneralPassword);
        System.out.println("레지스터 카카오 : " + kakaoGeneralPassword);
    	
        return "register_kakao"; // Return the name of your Thymeleaf template
    }
    
//    @PostMapping("/register_kakao")
//    public ModelAndView registerUserKakao(@RequestParam("email") String email, // Add other necessary parameters
//                                          @RequestParam("regionCode") Long regionCodeId) {
//        // Kakao registration logic should be implemented here
//        return new ModelAndView("redirect:/register_success");
//    }
    
    @GetMapping("/register")
    public String registerForm(Model model) {
    	
        model.addAttribute("user", new Users());  // Add this line if missing
        model.addAttribute("regionCodes", rd.findAll()); // Assuming regions are needed for the form
        return "register"; // This should be the path to your registration form view
    }
    
    @PostMapping("/registerSubmit")
    public String registerSubmit(@ModelAttribute("user") Users user,
                                 @RequestParam("uploadFile") MultipartFile uploadFile,
                                 @RequestParam("regionCode") String regionCode, // To handle region code from form
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        System.out.println("포스트매핑작동");

        // Set AuthType to STANDARD always
        user.setAuthType(AuthType.STANDARD);
        
        System.out.println(user.getNickname());
        System.out.println(user.getName());
        System.out.println(user.getEmail());
        System.out.println(user.getPhone());
        System.out.println(user.getPasswordHash());
        System.out.println(user.getRegionCode());
        System.out.println(user.getFilename());
        System.out.println(user.getAuthType());
        System.out.println(user.getId());
        System.out.println("1차 데이터 수집 종료");

        String rawPassword = user.getPasswordHash();
        if (rawPassword == null || rawPassword.isEmpty()) {
    	  String encodedPassword = passwordEncoder.encode(rawPassword);
          user.setPasswordHash(encodedPassword);
            return "redirect:/register";
        }
        
        // Encode password and set it to user
      
        System.out.println(user.getPasswordHash());
        System.out.println("2차 데이터 수집 종료");
        
//        // Validation checks
//        if (user.getName().isEmpty() || user.getNickname().isEmpty() || user.getPhone().isEmpty() || !passwordEncoder.matches(request.getParameter("password"), user.getPasswordHash())) {
//            redirectAttributes.addFlashAttribute("error", "Validation error: Please check your inputs.");
//            System.out.println("발리데이션통과");
//            return "redirect:/register"; // Keeping the original redirect
//        }

        
        // Handling file upload
        try {
        	System.out.println("파일 기록 시작");
            if (uploadFile != null && !uploadFile.isEmpty()) {
                String filename = uploadFile.getOriginalFilename();
                String uploadDir = Paths.get("src/main/resources/static/images").toAbsolutePath().toString();
                File targetFile = new File(uploadDir, filename);
                if (!targetFile.exists()) {
                    targetFile.getParentFile().mkdirs();
                    uploadFile.transferTo(targetFile);
                    user.setFilename(filename);
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "File upload failed: " + e.getMessage());
            System.out.println("파일업로드 실패");
            return "redirect:/register"; // Keeping the original redirect
        }

        // Add user with specified regionCode
        try {
            userService.addUser(user, regionCode);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            System.out.println("입력실패");
            return "redirect:/register"; // Keeping the original redirect
        }

        System.out.println("입력완료");
        return "redirect:/login"; // Redirecting to login upon success
    }
    
    @PostMapping("/registerKakaoSubmit")
    public String registerKakaoSubmit(@ModelAttribute("user") Users user,
						    		@RequestParam("uploadFile") MultipartFile uploadFile,
						            @RequestParam("nickname") String nickname,
						            @RequestParam("email") String email,
						            @RequestParam("phone") String phone,
						            @RequestParam("name") String name,
						            @RequestParam("regionCode") String regionCode,
						            @RequestParam("password") String rawPassword,
	                                 HttpServletRequest request,
	                                 RedirectAttributes redirectAttributes) {
        System.out.println("포스트매핑작동");
        
//        String nickname = (String) request.getAttribute("nickname");
//        String email = (String) request.getAttribute("email");
//        String phone = (String) request.getAttribute("phone");
//        String name = (String) request.getAttribute("name");
//        String regionCode = (String) request.getAttribute("regionCode");
        
        
     // Debug prints
        System.out.println("Received nickname: " + nickname);
        System.out.println("Received email: " + email);
        System.out.println("Received phone: " + phone);
        System.out.println("Received name: " + name);
        System.out.println("Received password: " + rawPassword);

        // Set AuthType to Kakao always
        user.setAuthType(AuthType.KAKAO);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setName(name);
        user.setPhone(phone);
        
        System.out.println(user.getNickname());
        System.out.println(user.getName());
        System.out.println(user.getEmail());
        System.out.println(user.getPhone());
        System.out.println(user.getRegionCode());
        System.out.println(user.getFilename());
        System.out.println(user.getAuthType());
        System.out.println(user.getId());
        System.out.println("1차 데이터 수집 종료");
        
        
//      String rawPassword = (String) request.getAttribute("password");
        if (rawPassword != null && !rawPassword.isEmpty()) {
            String encodedPassword = passwordEncoder.encode(rawPassword);
            user.setPasswordHash(encodedPassword);
        } else {
            return "redirect:/register_kakao";
        }

        // Encode password and set it to user
        System.out.println(user.getPasswordHash());
        System.out.println("2차 데이터 수집 종료");
        
        // Handling file upload
        try {
        	System.out.println("파일 기록 시작");
            if (uploadFile != null && !uploadFile.isEmpty()) {
                String filename = uploadFile.getOriginalFilename();
                String uploadDir = Paths.get("src/main/resources/static/images").toAbsolutePath().toString();
                File targetFile = new File(uploadDir, filename);
                if (!targetFile.exists()) {
                    targetFile.getParentFile().mkdirs();
                    uploadFile.transferTo(targetFile);
                    user.setFilename(filename);
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "File upload failed: " + e.getMessage());
            System.out.println("파일업로드 실패");
            return "redirect:/register_kakao"; // Keeping the original redirect
        }

        // Add user with specified regionCode
        try {
            userService.addUser(user, regionCode);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            System.out.println("입력실패");
            return "redirect:/register_kakao"; // Keeping the original redirect
        }

        System.out.println("입력완료");
        return "redirect:/login"; // Redirecting to login upon success
    }
}   
//    @PostMapping("/registerSubmit")
//    public String registerSubmit(@ModelAttribute Users user, 
//                                 @RequestParam("uploadFile") MultipartFile uploadFile, 
//                                 @RequestParam("regionCode") String regionCode, 
//                                 HttpServletRequest request, 
//                                 RedirectAttributes redirectAttributes) {
//    	System.out.println("포스트매핑작동");
//        // Set AuthType to STANDARD always
//        user.setAuthType(AuthType.STANDARD);
//
//        // Validation checks
//        if (user.getName().isEmpty() || user.getNickname().isEmpty() || user.getPhone().isEmpty() || !passwordEncoder.matches(request.getParameter("password"), user.getPasswordHash())) {
//            redirectAttributes.addFlashAttribute("error", "Validation error: Please check your inputs.");
//            System.out.println("발리데이션통과");
//            return "redirect:/register";
//        }
//        System.out.println(user.getEmail());
//        System.out.println(user.getFilename());
//        System.out.println(user.getNickname());
//        System.out.println(user.getName());
//        System.out.println(user.getPhone());
//        System.out.println(user.getAuthType());
//        System.out.println(user.getRegionCode());
//        System.out.println(user.getId());
//        
//        // Handling file upload
//        try {
//            if (uploadFile != null && !uploadFile.isEmpty()) {
//                String filename = uploadFile.getOriginalFilename();
//                String uploadDir = Paths.get("src/main/resources/static/images").toAbsolutePath().toString();
//                File targetFile = new File(uploadDir, filename);
//                if (!targetFile.exists()) {
//                    targetFile.getParentFile().mkdirs();
//                    uploadFile.transferTo(targetFile);
//                    user.setFilename(filename);
//                }
//            }
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "File upload failed: " + e.getMessage());
//        	System.out.println("파일업로드 실패");
//            return "redirect:/register";
//        }
//
//        // Add user with specified regionCode
//        try {
//            userService.addUser(user, regionCode);
//        } catch (RuntimeException e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//        	System.out.println("입력실패");
//            return "redirect:/register";
//        }
//    	System.out.println("입력완료");
//        return "redirect:/login";
//    }

//    @PostMapping("/registerSubmit")
//    public String registerSubmit(@ModelAttribute Users user, 
//                                 @RequestParam("uploadFile") MultipartFile uploadFile, 
//                                 @RequestParam("regionCode") String regionCode, // Add this to capture region code from the form
//                                 HttpServletRequest request, 
//                                 RedirectAttributes redirectAttributes) {
//        // Validation checks
//        if (user.getName().isEmpty() || user.getNickname().isEmpty() || user.getPhone().isEmpty() || !passwordEncoder.matches(request.getParameter("confirmPassword"), user.getPasswordHash())) {
//            redirectAttributes.addFlashAttribute("error", "Validation error: Please check your inputs.");
//            return "redirect:/register";
//        }
//
//        // Handling file upload
//        try {
//            if (uploadFile != null && !uploadFile.isEmpty()) {
//                String filename = uploadFile.getOriginalFilename();
//                String uploadDir = "/path/to/uploads"; // Change this to a writable directory
//                File targetFile = new File(uploadDir, filename);
//                if (!targetFile.exists()) {
//                    targetFile.getParentFile().mkdirs();
//                    uploadFile.transferTo(targetFile);
//                    user.setFilename(filename);
//                }
//            }
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "File upload failed: " + e.getMessage());
//            return "redirect:/register";
//        }
//
//        // Encode password and save the user with the region code
//        try {
//            userService.addUser(user, regionCode);
//        } catch (RuntimeException e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            return "redirect:/register";
//        }
//
//        return "redirect:/login";
//    }
    

