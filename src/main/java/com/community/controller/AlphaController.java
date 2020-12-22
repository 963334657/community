package com.community.controller;


import com.sun.javafx.collections.MappingChange;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "hello spring boot";
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration=request.getHeaderNames();
        while (enumeration.hasMoreElements()){
            String name=enumeration.nextElement();
            String value=request.getHeader(name);
            System.out.println(name+":"+value);
        }
        System.out.println(request.getParameter("code"));

        response.setContentType("text/html;charset=utf-8");
        try(
                PrintWriter writer=response.getWriter()
                ){

            writer.write("<h1>牛客网<h1/>");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @RequestMapping(path="/students" , method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(@RequestParam(name = "current",required = false,defaultValue = "1") int current,
                              @RequestParam(name = "limit",required = false,defaultValue = "10") int limit){
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }
    @RequestMapping(path="/student/{id}" , method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "some student";
    }

    @RequestMapping(path = "student" ,method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name,int age){
        System.out.println(name);
        System.out.println(age);

        return "success";
    }

    @RequestMapping(path = "teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView modelAndView=new ModelAndView();
        modelAndView.addObject("name","zhangsan");
        modelAndView.addObject("age",13);
        modelAndView.setViewName("/demo/view");
        return modelAndView;


    }

    @RequestMapping(path = "school",method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name","北京大闸蟹");
        model.addAttribute("age",80);
        return "/demo/view";
    }

    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    @ResponseBody
    public int getEmp(){

        return 5;
    }


}
