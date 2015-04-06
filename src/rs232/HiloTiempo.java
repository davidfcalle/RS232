package rs232;

public class HiloTiempo extends Thread implements Runnable {
    
	private Integer tiempo;
	
	public HiloTiempo(Integer tiempo){
	this.tiempo=tiempo;	
	}
	
	public Integer getTiempo(){
		return tiempo;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(tiempo<=100){
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		tiempo++;
		}
		//System.out.println("fin hilo tiempo "+tiempo);
	}

}
