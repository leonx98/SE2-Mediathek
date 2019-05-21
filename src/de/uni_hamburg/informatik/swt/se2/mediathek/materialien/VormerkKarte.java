package de.uni_hamburg.informatik.swt.se2.mediathek.materialien;

import java.util.ArrayList;
import java.util.List;
import de.uni_hamburg.informatik.swt.se2.mediathek.materialien.medien.Medium;

/**
 * Eine Vormerkkarte zum vormerken von Medien
 * 
 * @author Division durch Null
 * @version SoSe 2019
 */

public class VormerkKarte {
	
	// private variables
	private Medium _medium;
	private List<Kunde> _kunden;
	
	// Konstruktoren
	/**
	 * Konstruktor für eine VormerkKarte 
	 * 
	 * @param medium Das vorgemerkte Medium
	 * @param kunden Eine Liste von Kunden, die vormerken
	 * 
	 * @require medium != null
	 * @require kunden != null
	 * 
	 * @ensure getAnzahlVormerker() == _kunden.size()
	 * @ensure getVormerker(int index) == _kunden.get(index)
	 * @ensure getMedium() = _medium
	 */
	public VormerkKarte(Medium medium, List<Kunde> kunden) {
		assert medium != null : "Vorbedingung verletzt";
		assert kunden != null : "Vorbedingung verletzt";
		
		_medium = medium;
		_kunden = kunden;
	}
	
	/**
	 * Konstruktor für eine VormerkKarte
	 * 
	 * @param medium Das vorgemerkte Medium
	 * @param kunden Ein Kunden, der vormerkt
	 * 
	 * @require medium != null
	 * @require kunde != null
	 * 
	 * @ensure getAnzahlVormerker() == _kunden.size()
	 * @ensure getVormerker(int index) == _kunden.get(index)
	 * @ensure getMedium() = _medium
	 */
	public VormerkKarte(Medium medium, Kunde kunde) {
		assert medium != null : "Vorbedingung verletzt";
		assert kunde != null : "Vorbedingung verletzt";
		
		_medium = medium;
		
		List<Kunde> kunden = new ArrayList<Kunde>();
		kunden.add(kunde);
		_kunden = kunden;
	}

	// Methoden
	/**
	 * Gibt die Anzahl der Vormerker zurück
	 * 
	 * @ensure 3 >= result >= 1
	 * 
	 * @return Anzahl der Vormerker
	 */
	public int getAnzahlVormerker() {
		return _kunden.size();
	}
	
	/**
	 * Gibt Vormerker an der Position index zurück
	 * 
	 * @require getAnzahlVormerker() >= index >= 1
	 * 
	 * @ensure result != null
	 * 
	 * @return Kunde an der angegebenen Position
	 */
	public Kunde getVormerker(int index) {
		// decrement because range is 0 .. 2
		index--;
		if (index >= 0 && index <= 2) {
			if (index >= 0 && index <= getAnzahlVormerker()-1) {
				return _kunden.get(index);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Gibt das Medium der Vormerkkarte zurück
	 * 
	 * @ensure result != null
	 * 
	 * @return Das Medium
	 */
	public Medium getMedium() {
		return _medium;
	}
	
	/**
	 * Gibt das Medium der Vormerkkarte zurück
	 * 
	 * @required kunde != null
	 */
	public void setVormerker(Kunde kunde) {
		assert kunde != null : "Vorbedingung verletzt: kunde = null";
		
		if (_kunden.size() < 3) {
			_kunden.add(kunde);
		}
	}
	
	/**
	 * Entfernt den ersten Vormerker (alle anderen rücken um einen vor)
	 * 
	 */
	public void removeFirstVormerker() {
		if (_kunden.size() >= 1) {
			_kunden.remove(0);
		}
	}
}
