package org.karthunter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import io.magicthegathering.javasdk.api.CardAPI;
import io.magicthegathering.javasdk.resource.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KartHunterTest{

    @BeforeEach
    void setUp(){
        KartHunter.decks.clear();
        KartHunter.card = null;
        KartHunter.initializeDecks();
    }

    @Test
    void initializeDecks(){
        assertEquals(5, KartHunter.decks.size(), "5 decks should be initialized upon boot");
        assertTrue(KartHunter.decks.get(0) != null,"Deck at index 0 should not be null");
        assertTrue(KartHunter.decks.get(1) != null,"Deck at index 1 should not be null");
        assertTrue(KartHunter.decks.get(2) != null,"Deck at index 2 should not be null");
        assertTrue(KartHunter.decks.get(3) != null,"Deck at index 3 should not be null");
        assertTrue(KartHunter.decks.get(4) != null,"Deck at index 4 should not be null");
    }
    @Test
    public void testFetchCards(){
        List<String> filter = new ArrayList<>(Collections.singleton("name=" + "Air Elemental"));
        List<Card> testCardList = KartHunter.fetchCards(filter);
        Card testCard = new Card();
        testCard.setName("Air Elemental");
        assertEquals(testCardList.get(0).getName(), testCard.getName());
        testCard = CardAPI.getCard( 409574); // exact multiverse ID of strip mine to compare to my filter
        filter.clear();
        filter = new ArrayList<>(Collections.singleton("name=" + "Strip Mine"));
        testCardList = KartHunter.fetchCards(filter);
        assertEquals(testCardList.get(0).getType(),testCard.getType()); // Tests fetching using multiverse ID vs my search method, this time comparing types.
    }


    @Test
    void addCardToDeck(){
        Card testCard = new Card();
        testCard.setName("Aesi");
        KartHunter.decks.get(0).clear();
        InputStream inStr = System.in;
        String str = "1\n";
        ByteArrayInputStream bAIS = new ByteArrayInputStream(str.getBytes());
        System.setIn(bAIS);
        KartHunter.console = new Scanner(System.in);
        KartHunter.addCardToDeck(testCard);
        Card testCard2 = new Card();
        testCard2.setName("Aesi");
        assertTrue(KartHunter.decks.get(0).size() == 1,"Size should increase after adding card");
        System.setIn(inStr);
    }

    @Test
    void testRemoveCardFromDeck(){
        Card testCard = new Card();
        testCard.setName("Aesi");
        KartHunter.decks.get(0).clear();
        InputStream inStr = System.in;
        String str = "1\n0\n";
        ByteArrayInputStream bAIS = new ByteArrayInputStream(str.getBytes());
        System.setIn(bAIS);
        KartHunter.console = new Scanner(System.in);
        KartHunter.decks.get(0).add(testCard);
        assertTrue(KartHunter.decks.get(0).size() == 1);
        KartHunter.removeCardFromDeck(KartHunter.decks.get(0));
        assertTrue(KartHunter.decks.get(0).size() == 0);
        System.setIn(inStr);
    }

    @Test
    void detailedInspect(){
        Card testCard = CardAPI.getCard(500791); //Aesi multivereID
        KartHunter.card = testCard;
        InputStream inStr = System.in;
        String str = "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n0\n0\n";
        ByteArrayInputStream bAIS = new ByteArrayInputStream(str.getBytes());
        System.setIn(bAIS);
        KartHunter.console = new Scanner(System.in);
        KartHunter.detailedInspect();
        System.setIn(inStr);
    }
}