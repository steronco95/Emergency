package it.polito.tdp.Emergency.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import it.polito.tdp.Emergency.model.Event;
import it.polito.tdp.Emergency.model.Event.EventType;
import it.polito.tdp.Emergency.model.Paziente.CodiceColore;

public class Simulator {

	//PARAMETRI DI SIMULAZIONE
	private int NS = 5; //numero studi medici
	
	private int NP  = 150; //numero di pazienti
	private Duration T_ARRIVAL = Duration.ofMinutes(10); //intervallo tra pazienti
	
	
	private final Duration DURATION_TRIAGE  = Duration.ofMinutes(5);
	private final Duration DURATION_WHITE  = Duration.ofMinutes(10);
	private final Duration DURATION_YELLOW  = Duration.ofMinutes(15);
	private final Duration DURATION_RED  = Duration.ofMinutes(30);
	
	private final Duration TIMEOUT_WHITE  = Duration.ofMinutes(90);
	private final Duration TIMEOUT_YELLOW  = Duration.ofMinutes(30);
	private final Duration TIMEOUT_RED  = Duration.ofMinutes(60);

	private final LocalTime oraInizio = LocalTime.of(8,0);
	private final LocalTime oraFine = LocalTime.of(20, 0);
	
	private final Duration TICK_TIME = Duration.ofMinutes(5);
	
	//OUTPUT DA CALCOLARE
	private int pazientiTot;
	private int pazientiDimessi;
	private int pazientiAbbandonano;
	private int pazientiMorti;
	
	//STATO SISTEMA
	private List<Paziente> pazienti;
	private PriorityQueue<Paziente> attesa;
	private int studiLiberi;
	
	private  CodiceColore coloreAssegnato;
	
	//CODA DEGLI EVENTI
	private PriorityQueue<Event> coda;
	
	
	//INIZIALIZZAZIONE DELLA SIMULAZIONE 
	public void init() {
		this.coda = new PriorityQueue<>();
		this.pazienti = new ArrayList<>();
		this.pazientiTot = this.pazientiDimessi = this.pazientiMorti = this.pazientiAbbandonano = 0;
		this.coloreAssegnato = CodiceColore.WHITE;
		this.attesa = new PriorityQueue<>();
		this.studiLiberi = this.NS;
		//GENERO EVENTI INIZIALI
		int nPaz = 0;
		LocalTime oraArrivo = this.oraInizio;
		
		while(nPaz< this.NP && oraArrivo.isBefore(this.oraFine)) {
			Paziente p = new Paziente(oraArrivo, CodiceColore.UNKNOWN);
			
			this.pazienti.add(p);
		
			Event e = new Event(oraArrivo,EventType.ARRIVAL,p);
			coda.add(e);
			
			nPaz++;
			oraArrivo = oraArrivo.plus(T_ARRIVAL);
		}
		
		this.coda.add(new Event(this.oraInizio, EventType.TICK, null));
	}
	
	
	
	//ESECUZIONE 
	public void run() {
		while(!coda.isEmpty()) {
			Event e = this.coda.poll();
			System.out.println(e);
			processEvent(e);
		}
		
	}
	
	public void processEvent(Event e) {
		
		Paziente p = e.getPaziente();
		
		switch(e.getType()) {
		
		case ARRIVAL: //arrivo nuovo paziente, lo mando in triage e tra 5 minuti sara finito il triage
			
			coda.add(new Event(e.getTime().plus(DURATION_TRIAGE),EventType.TRIAGE,p));
			this.pazientiTot++;
			
			break;
		
		case TRIAGE: //assegno un codice colore ad un paziente e lo metto in lista d'attesa, schedula eventuali timeout
			p.setColore(nuovoCodiceColore());
			
			attesa.add(p);
			
			if(p.getColore() == CodiceColore.WHITE)
			coda.add(new Event(e.getTime().plus(TIMEOUT_WHITE), EventType.TIMEOUT,p ));
			else if(p.getColore() == CodiceColore.YELLOW)
				coda.add(new Event(e.getTime().plus(TIMEOUT_YELLOW), EventType.TIMEOUT,p ));
			else if(p.getColore() == CodiceColore.RED)
				coda.add(new Event(e.getTime().plus(TIMEOUT_RED), EventType.TIMEOUT,p ));
			
			
			break;
		
		case FREE_STUDIO:
			if(this.studiLiberi==0) // non ci sono studi liberi
				break ;
			Paziente prossimo = attesa.poll();
			if(prossimo != null) {
				//LO FACCIO ENTRARE
				this.studiLiberi--;
				//SCHEDULA L'USCITA DALLO STUDIO
				if(prossimo.getColore() == CodiceColore.WHITE)
					coda.add(new Event(e.getTime().plus(DURATION_WHITE), EventType.TREATED,prossimo ));
					else if(prossimo.getColore() == CodiceColore.YELLOW)
						coda.add(new Event(e.getTime().plus(DURATION_YELLOW), EventType.TREATED,prossimo ));
					else if(prossimo.getColore() == CodiceColore.RED)
						coda.add(new Event(e.getTime().plus(DURATION_RED), EventType.TREATED,prossimo ));
				
			}
			
			break;
			
		case TREATED:
			//libero lo studio
			this.studiLiberi++;
			this.pazientiDimessi++;
			p.setColore(CodiceColore.OUT);
			this.coda.add(new Event(e.getTime(),EventType.FREE_STUDIO,null));
			
			break;
			
		case TIMEOUT:
			//ESCI DALLA LISTA D'ATTESA
			attesa.remove(p);
			if(p.getColore() == CodiceColore.OUT)
				break;
			switch(p.getColore()) {
			
			case WHITE:
				//VAI A CASA 
				this.pazientiAbbandonano++;
				break;
			
			case YELLOW:
				//DIVENTA RED
				p.setColore(CodiceColore.RED);
				attesa.add(p);
				break;
			case RED:
				//MUORE
				this.pazientiMorti++;
				break;
				
			
			}
			
			break;
		
		case TICK:
			if(this.studiLiberi >0) {
				//schedulo un FREE_STUDIO
				
				this.coda.add(new Event(this.oraInizio,EventType.FREE_STUDIO,null));
			}
		
			if(e.getTime().isBefore(LocalTime.of(23, 30)))
				this.coda.add(new Event(e.getTime().plus(this.TICK_TIME),
					EventType.TICK, null));
			
			break;
		}
		
		
			
			
		
	}
	
	
	private CodiceColore nuovoCodiceColore() {
		CodiceColore nuovo = coloreAssegnato;
		
		if(coloreAssegnato == CodiceColore.WHITE) {
			coloreAssegnato = CodiceColore.YELLOW;
		}else if (coloreAssegnato == CodiceColore.YELLOW) {
			coloreAssegnato = CodiceColore.RED;
		}else {
			coloreAssegnato = CodiceColore.WHITE;
		}
		
		return coloreAssegnato;
	}



	public int getNS() {
		return NS;
	}
	public void setNS(int nS) {
		NS = nS;
	}
	public int getNP() {
		return NP;
	}
	public void setNP(int nP) {
		NP = nP;
	}
	public Duration getT_ARRIVAL() {
		return T_ARRIVAL;
	}
	public void setT_ARRIVAL(Duration t_ARRIVAL) {
		T_ARRIVAL = t_ARRIVAL;
	}



	public int getPazientiTot() {
		// TODO Auto-generated method stub
		return this.pazientiTot;
	}



	public int getPazientiDimessi() {
		// TODO Auto-generated method stub
		return this.pazientiDimessi;
	}



	public int getPazientiMorti() {
		// TODO Auto-generated method stub
		return this.pazientiMorti;
	}



	public int getPazientiAbbandonano() {
		// TODO Auto-generated method stub
		return this.pazientiAbbandonano;
	}
	
	
	
}
