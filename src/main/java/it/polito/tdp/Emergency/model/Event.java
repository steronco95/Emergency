package it.polito.tdp.Emergency.model;

import java.time.LocalTime;

public class Event implements Comparable<Event> {

	
	public enum EventType{
		ARRIVAL,TRIAGE,FREE_STUDIO,TREATED,TIMEOUT,TICK;
	}
	
	
	private LocalTime time;
	private EventType type;
	private Paziente paziente;
	
	
	
	public Event(LocalTime time, EventType type, Paziente paziente) {
		super();
		this.time = time;
		this.type = type;
		this.paziente = paziente;
	}

	
	public LocalTime getTime() {
		return time;
	}

	public EventType getType() {
		return type;
	}
	
	public Paziente getPaziente() {
	
		return this.paziente;
	}


	@Override
	public int compareTo(Event o) {
		
		return this.time.compareTo(o.time);
	}


	@Override
	public String toString() {
		return "Event [time=" + time + ", type=" + type + ", paziente=" + paziente + "]";
	}
	
	
	
	
}
