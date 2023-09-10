package com.postinforg.annoscoreapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.postinforg.annoscoreapi.service.ManageService;

@Controller
public class MainController extends CommonController {

	@Autowired ManageService manageService;
	
	@GetMapping("/index")
	public String index() throws Exception {
		int res = manageService.test(null);
		System.out.println(">>> res : " + res);
		return "home";
	}

}
