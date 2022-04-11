package br.com.servidor;

import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DistribuirTarefas implements Runnable {

	private Socket socket;
	private ServidorTarefas servidor;
	private ExecutorService threadPoll;
	private BlockingQueue<String> filaComandos;

	public DistribuirTarefas(ExecutorService threadPoll, BlockingQueue<String> filaComandos, Socket socket, ServidorTarefas servidor) {
		this.threadPoll = threadPoll;
		this.filaComandos = filaComandos;
		this.socket = socket;
		this.servidor = servidor;
	}

	@Override
	public void run() {
		try {		
			System.out.println("Distribuindo tarefas para " + socket);
			
			Scanner entradaCliente = new Scanner(socket.getInputStream());
			PrintStream saidaCliente = new PrintStream(socket.getOutputStream());
			
			while (entradaCliente.hasNextLine()) {
				String comando = entradaCliente.nextLine();
				System.out.println("Comando recebido" + comando);
				
				switch (comando) {
				case "c1": {
					saidaCliente.println("Confirmação comando 1");
					ComandoC1 c1 = new ComandoC1(saidaCliente);
					this.threadPoll.execute(c1);
					break;
				}
				case "c2": {
					saidaCliente.println("Confirmação comando 2");
					ComandoC2ChamaWS c2WS = new ComandoC2ChamaWS(saidaCliente);
					ComandoC2AcessaBanco c2Banco = new ComandoC2AcessaBanco(saidaCliente);
					Future<String> futureWS = this.threadPoll.submit(c2WS);
					Future<String> futureBanco = this.threadPoll.submit(c2Banco);
					
					this.threadPoll.submit(new JuntaResultadosFutureWSFutureBanco(futureWS, futureBanco, saidaCliente));
					
					break;
				}
				case "c3": {
					this.filaComandos.put(comando); //bloqueia
					saidaCliente.println("Comando C3 adicionado na fila");
					break;
				}
				case "fim": {
					saidaCliente.println("Desligando o servidor");
					servidor.parar();
					break;
				}
				default:
					saidaCliente.println("Comando não encontrado");
					break;
				}
			}
			saidaCliente.close();
			entradaCliente.close();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

}
