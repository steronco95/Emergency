package it.polito.tdp.Emergency.model;

import java.time.LocalTime;

/**
 * Rappresenta le informazioni su ciascun paziente nel sistema 
 * @author djonk
 *
 */
public class Paziente implements Comparable<Paziente> {

	public enum CodiceColore{
		UNKNOWN,//VALORE DEFAULT PRE-TRIAGE
		WHITE,YELLOW,RED,BLACK, OUT; //VALORI ASSEGNATI DOPO IL TRIAGE
	}
	
	private LocalTime oraArrivo;
	private CodiceColore colore;
	
	public Paziente(LocalTime oraArrivo, CodiceColore colore) {
		super();
		this.oraArrivo = oraArrivo;
		this.colore = colore;
	}

	public CodiceColore getColore() {
		return colore;
	}

	public void setColore(CodiceColore colore) {
		this.colore = colore;
	}

	public LocalTime getOraArrivo() {
		return oraArrivo;
	}

	@Override
	public int compareTo(Paziente o) {
		
		if(this.colore == o.colore) {
			return this.oraArrivo.compareTo(o.oraArrivo);
		}else if (this.colore == CodiceColore.RED) {
			return -1;
		}else if(o.colore == CodiceColore.RED) {
			return +1;
		}else if (this.colore == CodiceColore.YELLOW) {
			return -1;
		}else if(o.colore == CodiceColore.YELLOW) {
			return +1;
		}
		
		throw new RuntimeException("Comparator<Paziente> failed");
	}

	@Override
	public String toString() {
		return "Paziente [oraArrivo=" + oraArrivo + ", colore=" + colore + "]";
	}
	
	
	
	
	
	
	
}
