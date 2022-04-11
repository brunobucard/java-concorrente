package br.com.servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServidorTarefas {
	
	private ServerSocket servidor;
	private ExecutorService threadPoll;
	//Atomic � uma classe atomica, que � thread-safe e n�o causa problema de concorr�ncia
	//N�o permite o cache de valor da vari�vel
	//Poderiamos tamb�m usar o modificador volative, que far� com que a vari�vel seja acessada diretamente da mem�ria principal
	private AtomicBoolean estaRodando;
	private BlockingQueue<String> filaComandos;

	public ServidorTarefas() throws IOException {
		System.out.println("--- Iniciando servidor ---");
		this.servidor = new ServerSocket(12345);
		this.threadPoll = Executors.newFixedThreadPool(4, new FabricaDeThreads());	//newCachedThreadPool();
		this.estaRodando = new AtomicBoolean(true);
		this.filaComandos = new ArrayBlockingQueue<>(2);
	}

	public static void main(String[] args) throws Exception {
		ServidorTarefas servidor = new ServidorTarefas();
		servidor.rodar();
		servidor.parar();
	}

	public void parar() throws IOException {
		estaRodando.set(false);;
		servidor.close();
		threadPoll.shutdown();		
	}

	public void rodar() throws IOException {
		while(this.estaRodando.get()) {
			try {
				Socket socket = servidor.accept();
				System.out.println("Aceitando novo cliente na porta " + socket.getPort());
				
				DistribuirTarefas distribuirTarefas = new DistribuirTarefas(threadPoll,filaComandos, socket, this);
				threadPoll.execute(distribuirTarefas);
			} catch (SocketException e) {
				System.out.println("SocketException, Est� rodando? " + this.estaRodando);
			}			
		}		
	}

}
