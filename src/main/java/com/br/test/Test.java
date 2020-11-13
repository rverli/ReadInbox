package com.br.test;

import java.io.IOException;

import com.br.test.email.Email;

public class Test {
	
	public static void main(String[] args) {
		
		try {
			new Email().execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
