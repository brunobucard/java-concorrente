package br.com.servidor;

import java.lang.Thread.UncaughtExceptionHandler;

public class TratadorDeExcecao implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread thread, Throwable e) {
		System.out.println("Deu excecao na thread " + thread.getName() + " " + e.getMessage());
	}

}
