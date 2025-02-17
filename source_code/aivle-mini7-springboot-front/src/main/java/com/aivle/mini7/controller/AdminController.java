package com.aivle.mini7.controller;


import com.aivle.mini7.service.LogService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;   
import com.aivle.mini7.dto.LogDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    private final LogService logService;
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "1234";

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("adminAuthenticated", true);
            return "redirect:/admin";
        }
        return "redirect:/admin/login?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    @GetMapping("")
    public ModelAndView index(
            HttpSession session,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String urgencyLevel) {
        // 세션 체크
        Boolean isAuthenticated = (Boolean) session.getAttribute("adminAuthenticated");
        if (isAuthenticated == null || !isAuthenticated) {
            return new ModelAndView("redirect:/admin/login");
        }

        Page<LogDto.ResponseList> logList = logService.getLogList(pageable, startDate, endDate, urgencyLevel);
        ModelAndView mv = new ModelAndView("admin/index");
        mv.addObject("logList", logList);
        mv.addObject("stats", logService.getDashboardStats());
        return mv;
    }

    @PostMapping("/updateTime")
    @ResponseBody
    public ResponseEntity<?> updateActualTime(
            @RequestParam Long id,
            @RequestParam Integer actualTime,
            @RequestParam String type) {  // "hospital" or "firestation"
        
        logService.updateActualTime(id, actualTime, type);
        return ResponseEntity.ok().build();
    }
}
