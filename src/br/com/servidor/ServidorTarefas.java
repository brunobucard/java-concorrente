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
	//Atomic é uma classe atomica, que é thread-safe e não causa problema de concorrência
	//Não permite o cache de valor da variável
	//Poderiamos também usar o modificador volative, que fará com que a variável seja acessada diretamente da memória principal
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
				System.out.println("SocketException, Está rodando? " + this.estaRodando);
			}			
		}		
	}

}
