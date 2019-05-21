package de.uni_hamburg.informatik.swt.se2.mediathek.services.verleih;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.uni_hamburg.informatik.swt.se2.mediathek.fachwerte.Datum;
import de.uni_hamburg.informatik.swt.se2.mediathek.fachwerte.Kundennummer;
import de.uni_hamburg.informatik.swt.se2.mediathek.materialien.Kunde;
import de.uni_hamburg.informatik.swt.se2.mediathek.materialien.Verleihkarte;
import de.uni_hamburg.informatik.swt.se2.mediathek.materialien.medien.CD;
import de.uni_hamburg.informatik.swt.se2.mediathek.materialien.medien.Medium;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.ServiceObserver;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.kundenstamm.KundenstammService;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.kundenstamm.KundenstammServiceImpl;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.medienbestand.MedienbestandService;
import de.uni_hamburg.informatik.swt.se2.mediathek.services.medienbestand.MedienbestandServiceImpl;

/**
 * @author SE2-Team
 */
public class VerleihServiceImplTest
{
    private Datum _datum;
    private Kunde _kunde;
    private VerleihService _service;
    private List<Medium> _medienListe;
    private Kunde _vormerkkunde;
    
    private Kunde _kunde1;
    private Kunde _kunde2;
    private Kunde _kunde3;
    private Kunde _kunde4;

    public VerleihServiceImplTest()
    {
        _datum = new Datum(3, 4, 2009);
        KundenstammService kundenstamm = new KundenstammServiceImpl(
                new ArrayList<Kunde>());
        _kunde = new Kunde(new Kundennummer(123456), "ich", "du");

        _vormerkkunde = new Kunde(new Kundennummer(666999), "paul", "panter");
        
        _kunde1 = new Kunde(new Kundennummer(101010), "i", "d");
    	_kunde2 = new Kunde(new Kundennummer(202020), "ih", "djdn");
    	_kunde3 = new Kunde(new Kundennummer(303030), "haha", "dr");
    	_kunde4 = new Kunde(new Kundennummer(404040), "uu", "dfff");

        kundenstamm.fuegeKundenEin(_kunde);
        kundenstamm.fuegeKundenEin(_vormerkkunde);
        kundenstamm.fuegeKundenEin(_kunde1);
        kundenstamm.fuegeKundenEin(_kunde2);
        kundenstamm.fuegeKundenEin(_kunde3);
        kundenstamm.fuegeKundenEin(_kunde4);
        
        MedienbestandService medienbestand = new MedienbestandServiceImpl(
                new ArrayList<Medium>());
        Medium medium = new CD("CD1", "baz", "foo", 123);
        medienbestand.fuegeMediumEin(medium);
        medium = new CD("CD2", "baz", "foo", 123);
        medienbestand.fuegeMediumEin(medium);
        medium = new CD("CD3", "baz", "foo", 123);
        medienbestand.fuegeMediumEin(medium);
        medium = new CD("CD4", "baz", "foo", 123);
        medienbestand.fuegeMediumEin(medium);
        _medienListe = medienbestand.getMedien();
        _service = new VerleihServiceImpl(kundenstamm, medienbestand,
                new ArrayList<Verleihkarte>());
    }

    @Test
    public void testeNachInitialisierungIstNichtsVerliehen() throws Exception
    {
        assertTrue(_service.getVerleihkarten()
            .isEmpty());
        assertFalse(_service.istVerliehen(_medienListe.get(0)));
        assertFalse(_service.sindAlleVerliehen(_medienListe));
        assertTrue(_service.sindAlleNichtVerliehen(_medienListe));
    }

    @Test
    public void testeVerleihUndRueckgabeVonMedien() throws Exception
    {
        // Lege eine Liste mit nur verliehenen und eine Liste mit ausschließlich
        // nicht verliehenen Medien an
        List<Medium> verlieheneMedien = _medienListe.subList(0, 2);
        List<Medium> nichtVerlieheneMedien = _medienListe.subList(2, 4);
        _service.verleiheAn(_kunde, verlieheneMedien, _datum);

        // Prüfe, ob alle sondierenden Operationen für das Vertragsmodell
        // funktionieren
        assertTrue(_service.istVerliehen(verlieheneMedien.get(0)));
        assertTrue(_service.istVerliehen(verlieheneMedien.get(1)));
        assertFalse(_service.istVerliehen(nichtVerlieheneMedien.get(0)));
        assertFalse(_service.istVerliehen(nichtVerlieheneMedien.get(1)));
        assertTrue(_service.sindAlleVerliehen(verlieheneMedien));
        assertTrue(_service.sindAlleNichtVerliehen(nichtVerlieheneMedien));
        assertFalse(_service.sindAlleNichtVerliehen(verlieheneMedien));
        assertFalse(_service.sindAlleVerliehen(nichtVerlieheneMedien));
        assertFalse(_service.sindAlleVerliehen(_medienListe));
        assertFalse(_service.sindAlleNichtVerliehen(_medienListe));
        assertTrue(_service.istVerliehenAn(_kunde, verlieheneMedien.get(0)));
        assertTrue(_service.istVerliehenAn(_kunde, verlieheneMedien.get(1)));
        assertFalse(
                _service.istVerliehenAn(_kunde, nichtVerlieheneMedien.get(0)));
        assertFalse(
                _service.istVerliehenAn(_kunde, nichtVerlieheneMedien.get(1)));
        assertTrue(_service.sindAlleVerliehenAn(_kunde, verlieheneMedien));
        assertFalse(
                _service.sindAlleVerliehenAn(_kunde, nichtVerlieheneMedien));

        // Prüfe alle sonstigen sondierenden Methoden
        assertEquals(2, _service.getVerleihkarten()
            .size());

        _service.nimmZurueck(verlieheneMedien, _datum);
        // Prüfe, ob alle sondierenden Operationen für das Vertragsmodell
        // funktionieren
        assertFalse(_service.istVerliehen(verlieheneMedien.get(0)));
        assertFalse(_service.istVerliehen(verlieheneMedien.get(1)));
        assertFalse(_service.istVerliehen(nichtVerlieheneMedien.get(0)));
        assertFalse(_service.istVerliehen(nichtVerlieheneMedien.get(1)));
        assertFalse(_service.sindAlleVerliehen(verlieheneMedien));
        assertTrue(_service.sindAlleNichtVerliehen(nichtVerlieheneMedien));
        assertTrue(_service.sindAlleNichtVerliehen(verlieheneMedien));
        assertFalse(_service.sindAlleVerliehen(nichtVerlieheneMedien));
        assertFalse(_service.sindAlleVerliehen(_medienListe));
        assertTrue(_service.sindAlleNichtVerliehen(_medienListe));
        assertTrue(_service.getVerleihkarten()
            .isEmpty());
    }

    @Test
    public void testVerleihEreignisBeobachter() throws ProtokollierException
    {
        final boolean ereignisse[] = new boolean[1];
        ereignisse[0] = false;
        ServiceObserver beobachter = new ServiceObserver()
        {
            @Override
            public void reagiereAufAenderung()
            {
                ereignisse[0] = true;
            }
        };
        _service.verleiheAn(_kunde,
                Collections.singletonList(_medienListe.get(0)), _datum);
        assertFalse(ereignisse[0]);

        _service.registriereBeobachter(beobachter);
        _service.verleiheAn(_kunde,
                Collections.singletonList(_medienListe.get(1)), _datum);
        assertTrue(ereignisse[0]);

        _service.entferneBeobachter(beobachter);
        ereignisse[0] = false;
        _service.verleiheAn(_kunde,
                Collections.singletonList(_medienListe.get(2)), _datum);
        assertFalse(ereignisse[0]);
    }
    
    @Test
    public void testeVormerkenVormerkenMoeglich() throws Exception {
    	
    	// teste, ob höchstens drei Vormerker gestattet sind
    	Medium medium = _medienListe.get(0);
    	assertTrue(_service.istVormerkenMoeglich(_kunde1, medium));
    	_service.vormerken(_kunde1, medium);
    	
    	assertTrue(_service.istVormerkenMoeglich(_kunde2, medium));
    	_service.vormerken(_kunde2, medium);
    	
    	assertTrue(_service.istVormerkenMoeglich(_kunde3, medium));
    	_service.vormerken(_kunde3, medium);
    	
    	assertFalse(_service.istVormerkenMoeglich(_kunde4, medium));
    	
    	
    	// teste, ob ein Kunde sein schon ausgeliehens Medium nochmals vormerken kann
    	List<Medium> verlieheneMedien = _medienListe.subList(1, 2);
    	_service.verleiheAn(_kunde, verlieheneMedien, _datum);
    	assertFalse(_service.istVormerkenMoeglich(_kunde, verlieheneMedien.get(0)));
    	
    	
    	// teste, ob ein weiterer Kunde unabhänging vom Verleihstatus ein Medium vormerken kann
    	assertTrue(_service.istVormerkenMoeglich(_vormerkkunde, verlieheneMedien.get(0)));
    	_service.vormerken(_vormerkkunde, verlieheneMedien.get(0));
    }
    
    @Test
    public void testeVormerken() {
    	Medium medium = _medienListe.get(0);
    	
    	// teste, ob ein Kunde ein noch nicht verliehenes/vorgemerktes Medium vormerken kann
    	assertTrue(_service.istVormerkenMoeglich(_vormerkkunde, medium));
    	_service.vormerken(_vormerkkunde, medium);
		assertTrue(_service.istVormerkKarteVorhanden(medium));
		assertNotNull(_service.getVormerkKarteFuer(medium));
		
		// teste, ob der gleiche Kunde das Medium nochmal vormerken kann
		assertFalse(_service.istVormerkenMoeglich(_vormerkkunde, medium));
    }
    
    @Test
    public void testeVormerkenUndAusleihen() throws Exception {
    	Medium medium = _medienListe.get(0);
    	
    	// teste, ob nur der erste Vormerker das Medium ausleihen kann
    	assertTrue(_service.istVormerkenMoeglich(_vormerkkunde, medium));
    	_service.vormerken(_vormerkkunde, medium);
	
    	assertTrue(_service.istVormerkenMoeglich(_kunde, medium));
    	_service.vormerken(_kunde, medium);
		
    	List<Medium> med = _medienListe.subList(0, 1);
		assertTrue(_service.istVerleihenMoeglich(_vormerkkunde, med));
		assertFalse(_service.istVerleihenMoeglich(_kunde, med));
		
		
		// teste, ob _kunde erser Vormerker wird, wenn _vormerkkunde das Medium ausleiht
		_service.verleiheAn(_vormerkkunde, med, _datum);
		assertFalse(_service.istVerleihenMoeglich(_kunde, med));
		
		assert(_service.getVormerkKarteFuer(medium).getVormerker(1) == _kunde);
    }
    
}
