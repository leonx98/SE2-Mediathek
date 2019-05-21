package de.uni_hamburg.informatik.swt.se2.mediathek.services.verleih;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_hamburg.informatik.swt.se2.mediathek.fachwerte.Datum;
import de.uni_hamburg.informatik.swt.se2.mediathek.materialien.Kunde;
import de.uni_hamburg.informatik.swt.se2.mediathek.materialien.Verleihkarte;
import de.uni_hamburg.informatik.swt.se2.mediathek.materialien.VormerkKarte;
import de.uni_hamburg.informatik.swt.se2.mediathek.materialien.medien.Medium;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.AbstractObservableService;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.kundenstamm.KundenstammService;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.medienbestand.MedienbestandService;

/**
 * Diese Klasse implementiert das Interface VerleihService. Siehe dortiger
 * Kommentar.
 * 
 * @author SE2-Team
 * @version SoSe 2019
 */
public class VerleihServiceImpl extends AbstractObservableService
        implements VerleihService
{
    /**
     * Diese Map speichert für jedes eingefügte Medium die dazugehörige
     * Verleihkarte. Ein Zugriff auf die Verleihkarte ist dadurch leicht über
     * die Angabe des Mediums möglich. Beispiel: _verleihkarten.get(medium)
     */
    private Map<Medium, Verleihkarte> _verleihkarten;

    /**
     * Der Medienbestand.
     */
    private MedienbestandService _medienbestand;

    /**
     * Der Kundenstamm.
     */
    private KundenstammService _kundenstamm;

    /**
     * Der Protokollierer für die Verleihvorgänge.
     */
    private VerleihProtokollierer _protokollierer;
    
    /**
     * Diese Map speichert für jedes eingefügte Medium die dazugehörige
     * Vormerkkarte. Ein Zugriff auf die Vormerkkarte ist dadurch leicht über
     * die Angabe des Mediums möglich. Beispiel: _vormerkkarten.get(medium)
     */
    private Map<Medium, VormerkKarte> _vormerkkarten;

    /**
     * Konstruktor. Erzeugt einen neuen VerleihServiceImpl.
     * 
     * @param kundenstamm Der KundenstammService.
     * @param medienbestand Der MedienbestandService.
     * @param initialBestand Der initiale Bestand.
     * 
     * @require kundenstamm != null
     * @require medienbestand != null
     * @require initialBestand != null
     */
    public VerleihServiceImpl(KundenstammService kundenstamm,
            MedienbestandService medienbestand,
            List<Verleihkarte> initialBestand)
    {
        assert kundenstamm != null : "Vorbedingung verletzt: kundenstamm  != null";
        assert medienbestand != null : "Vorbedingung verletzt: medienbestand  != null";
        assert initialBestand != null : "Vorbedingung verletzt: initialBestand  != null";
        _verleihkarten = erzeugeVerleihkartenBestand(initialBestand);
        _kundenstamm = kundenstamm;
        _medienbestand = medienbestand;
        _protokollierer = new VerleihProtokollierer();
        _vormerkkarten = new HashMap<Medium, VormerkKarte>();
    }

    /**
     * Erzeugt eine neue HashMap aus dem Initialbestand.
     */
    private HashMap<Medium, Verleihkarte> erzeugeVerleihkartenBestand(
            List<Verleihkarte> initialBestand)
    {
        HashMap<Medium, Verleihkarte> result = new HashMap<Medium, Verleihkarte>();
        for (Verleihkarte verleihkarte : initialBestand)
        {
            result.put(verleihkarte.getMedium(), verleihkarte);
        }
        return result;
    }

    @Override
    public List<Verleihkarte> getVerleihkarten()
    {
        return new ArrayList<Verleihkarte>(_verleihkarten.values());
    }

    @Override
    public boolean istVerliehen(Medium medium)
    {
        assert mediumImBestand(
                medium) : "Vorbedingung verletzt: mediumExistiert(medium)";
        return _verleihkarten.get(medium) != null;
    }

    @Override
    public boolean istVerleihenMoeglich(Kunde kunde, List<Medium> medien)
    {
        assert kundeImBestand(kunde) : "Vorbedingung verletzt: kundeImBestand(kunde)";
        assert medienImBestand(medien) : "Vorbedingung verletzt: medienImBestand(medien)";
        
        if(sindAlleNichtVerliehen(medien)) {
        	for (Medium medium : medien) {
            	if (istVormerkKarteVorhanden(medium)) {
            		VormerkKarte karte = getVormerkKarteFuer(medium);
            		if(!(karte.getVormerker(1).equals(kunde))) {
            			return false;
            		}
            	}
            }
        	return true;
        } else {
        	return false;
        }
    }

    @Override
    public void nimmZurueck(List<Medium> medien, Datum rueckgabeDatum)
            throws ProtokollierException
    {
        assert sindAlleVerliehen(
                medien) : "Vorbedingung verletzt: sindAlleVerliehen(medien)";
        assert rueckgabeDatum != null : "Vorbedingung verletzt: rueckgabeDatum != null";

        for (Medium medium : medien)
        {
            Verleihkarte verleihkarte = _verleihkarten.get(medium);
            _verleihkarten.remove(medium);
            _protokollierer.protokolliere(
                    VerleihProtokollierer.EREIGNIS_RUECKGABE, verleihkarte);
        }

        informiereUeberAenderung();
    }

    @Override
    public boolean sindAlleNichtVerliehen(List<Medium> medien)
    {
        assert medienImBestand(
                medien) : "Vorbedingung verletzt: medienImBestand(medien)";
        boolean result = true;
        for (Medium medium : medien)
        {
            if (istVerliehen(medium))
            {
                result = false;
            }
        }
        return result;
    }

    @Override
    public boolean sindAlleVerliehenAn(Kunde kunde, List<Medium> medien)
    {
        assert kundeImBestand(
                kunde) : "Vorbedingung verletzt: kundeImBestand(kunde)";
        assert medienImBestand(
                medien) : "Vorbedingung verletzt: medienImBestand(medien)";

        boolean result = true;
        for (Medium medium : medien)
        {
            if (!istVerliehenAn(kunde, medium))
            {
                result = false;
            }
        }
        return result;
    }

    @Override
    public boolean istVerliehenAn(Kunde kunde, Medium medium)
    {
        assert kundeImBestand(
                kunde) : "Vorbedingung verletzt: kundeImBestand(kunde)";
        assert mediumImBestand(
                medium) : "Vorbedingung verletzt: mediumImBestand(medium)";

        return istVerliehen(medium) && getEntleiherFuer(medium).equals(kunde);
    }

    @Override
    public boolean sindAlleVerliehen(List<Medium> medien)
    {
        assert medienImBestand(
                medien) : "Vorbedingung verletzt: medienImBestand(medien)";

        boolean result = true;
        for (Medium medium : medien)
        {
            if (!istVerliehen(medium))
            {
                result = false;
            }
        }
        return result;
    }

    @Override
    public void verleiheAn(Kunde kunde, List<Medium> medien, Datum ausleihDatum)
            throws ProtokollierException
    {
        assert kundeImBestand(kunde) : "Vorbedingung verletzt: kundeImBestand(kunde)";
        assert sindAlleNichtVerliehen(medien) : "Vorbedingung verletzt: sindAlleNichtVerliehen(medien) ";
        assert ausleihDatum != null : "Vorbedingung verletzt: ausleihDatum != null";
        assert istVerleihenMoeglich(kunde, medien) : "Vorbedingung verletzt:  istVerleihenMoeglich(kunde, medien)";

        for (Medium medium : medien)
        {
            Verleihkarte verleihkarte = new Verleihkarte(kunde, medium, ausleihDatum);

            _verleihkarten.put(medium, verleihkarte);
            _protokollierer.protokolliere(VerleihProtokollierer.EREIGNIS_AUSLEIHE, verleihkarte);
            
            if (istVormerkKarteVorhanden(medium)) {
        		VormerkKarte karte = getVormerkKarteFuer(medium);
        		karte.removeFirstVormerker();
        	}
        }
        // Was passiert wenn das Protokollieren mitten in der Schleife
        // schief geht? informiereUeberAenderung in einen finally Block?
        informiereUeberAenderung();
    }

    @Override
    public boolean kundeImBestand(Kunde kunde)
    {
        return _kundenstamm.enthaeltKunden(kunde);
    }

    @Override
    public boolean mediumImBestand(Medium medium)
    {
        return _medienbestand.enthaeltMedium(medium);
    }

    @Override
    public boolean medienImBestand(List<Medium> medien)
    {
        assert medien != null : "Vorbedingung verletzt: medien != null";
        assert !medien.isEmpty() : "Vorbedingung verletzt: !medien.isEmpty()";

        boolean result = true;
        for (Medium medium : medien)
        {
            if (!mediumImBestand(medium))
            {
                result = false;
                break;
            }
        }
        return result;
    }

    @Override
    public List<Medium> getAusgelieheneMedienFuer(Kunde kunde)
    {
        assert kundeImBestand(
                kunde) : "Vorbedingung verletzt: kundeImBestand(kunde)";
        List<Medium> result = new ArrayList<Medium>();
        for (Verleihkarte verleihkarte : _verleihkarten.values())
        {
            if (verleihkarte.getEntleiher()
                .equals(kunde))
            {
                result.add(verleihkarte.getMedium());
            }
        }
        return result;
    }

    @Override
    public Kunde getEntleiherFuer(Medium medium)
    {
        assert istVerliehen(
                medium) : "Vorbedingung verletzt: istVerliehen(medium)";
        Verleihkarte verleihkarte = _verleihkarten.get(medium);
        return verleihkarte.getEntleiher();
    }

    @Override
    public Verleihkarte getVerleihkarteFuer(Medium medium)
    {
        assert istVerliehen(
                medium) : "Vorbedingung verletzt: istVerliehen(medium)";
        return _verleihkarten.get(medium);
    }

    @Override
    public List<Verleihkarte> getVerleihkartenFuer(Kunde kunde)
    {
        assert kundeImBestand(
                kunde) : "Vorbedingung verletzt: kundeImBestand(kunde)";
        List<Verleihkarte> result = new ArrayList<Verleihkarte>();
        for (Verleihkarte verleihkarte : _verleihkarten.values())
        {
            if (verleihkarte.getEntleiher()
                .equals(kunde))
            {
                result.add(verleihkarte);
            }
        }
        return result;
    }
    
 // Vormerk feature
    /**
     * Gibt zurück, ob vormerken für den Kunden und das Medium möglich ist
     * 
     * @param kunde Der Kunde
     * @param medium Das Medium
     * 
     * @require kunde != null
     * @requre medium != null
     * 
     * @return true, falls vormerken möglich ist
     */
    @Override
    public boolean istVormerkenMoeglich(Kunde kunde, Medium medium) {
    	
    	assert kundeImBestand(kunde) : "Vorbedingung verletzt: kundeImBestand(kunde)";
    	assert mediumImBestand(medium) : "Vorbedingung verletzt: mediumImBestand(medium)";
    	
    	if (istVerliehen(medium)) {
    		if (getEntleiherFuer(medium).equals(kunde)) {
        		return false;
        	}
    	}
    	
    	if (istVormerkKarteVorhanden(medium)) {
    		VormerkKarte vormerkkarte = getVormerkKarteFuer(medium);
    		
        	if (vormerkkarte.getAnzahlVormerker() >= 3) {
        		return false;
        	}
        	
        	for(int i = 1; i <= vormerkkarte.getAnzahlVormerker(); i++) {
        		if (vormerkkarte.getVormerker(i).equals(kunde)) {
        			return false;
        		}
        	}
    	}
    	
    	return true;
    }
    
    /**
     * Gibt alle Vormerkkarten des angegebenen Kunden zurück
     * 
     * @param kunde Der Kunde
     * 
     * @require kunde != null
     * 
     * @return Liste aller Vormerkkarten des Kunden wenn vorhanden, ansonsten leere Liste
     */
    @Override
    public List<VormerkKarte> getVormerkKartenFuer(Kunde kunde) {
    	
    	assert kundeImBestand(kunde) : "Vorbedingung verletzt: kundeImBestand(kunde)";
    	
    	List<VormerkKarte> karten = new ArrayList<VormerkKarte>();
    	for(VormerkKarte karte : _vormerkkarten.values()) {
    		for(int i = 1; i <= 3; i++) {
        		if (karte.getVormerker(i).equals(kunde)) {
        			karten.add(karte);
        		}
        	}
    	}
    	
    	return karten;
    }
    
    /**
     * Gibt Vormerkkarte des angegebenen Mediums zurück
     * 
     * @param medium Das Medium
     * 
     * @require medium != null
     * 
     * @return Vormerkkarte des Mediums
     */
    @Override
    public VormerkKarte getVormerkKarteFuer(Medium medium) {
    	
    	assert istVormerkKarteVorhanden(medium) : "Vorbedingung verletzt: istVormerkKarteVorhanden(medium)";
    	
    	return _vormerkkarten.get(medium);
    }
    
    /**
     * Merkt ein Medium vor für einen Kunden
     * 
     * @param kunde Der Kunde
     * @param medium Das Medium
     * 
     * @require kunde != null
     * @require medium != null
     * 
     */
    @Override
    public void vormerken(Kunde kunde, Medium medium) {
    	
    	assert kundeImBestand(kunde) : "Vorbedingung verletzt: kundeImBestand(kunde)";
    	assert mediumImBestand(medium) : "Vorbedingung verletzt: mediumImBestand(medium)";
    	
    	if (istVormerkenMoeglich(kunde, medium)) {
    		if(istVormerkKarteVorhanden(medium)) {
    			VormerkKarte karte = getVormerkKarteFuer(medium);
    			karte.setVormerker(kunde);
    		} else {
    			VormerkKarte karte = new VormerkKarte(medium, kunde);
        		_vormerkkarten.put(medium, karte);
    		}
    	}
    }
    
    /**
     * Prüft, ob es eine Vormerkkarte gibt für das angegebene Medium
     * 
     * @param medium Das Medium
     * 
     * @require medium != null
     * 
     * @return True, wenn Vormerkkarte vorhanden
     */
    @Override
    public boolean istVormerkKarteVorhanden(Medium medium) {
    	
    	assert mediumImBestand(medium) : "Vorbedingung verletzt: mediumImBestand(medium)";
    	
    	return _vormerkkarten.get(medium) != null;
    }

}
