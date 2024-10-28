package org.karthunter;

/* MTG-SDK-JAVA imports */
import io.magicthegathering.javasdk.api.MTGAPI;
import io.magicthegathering.javasdk.api.CardAPI;
import io.magicthegathering.javasdk.api.SetAPI;
import io.magicthegathering.javasdk.resource.Card;
import io.magicthegathering.javasdk.resource.MtgSet;

/* Java Imports */
import java.util.*;
import java.io.IOException;

/**
 *  KartHunter class, logic for the program, should have broken it down into more classes.
 *  Allows users to search for magic the gathering cards, build and manage a deck, and inspect the cards in greater detail, doesn't save or load the decks from anywhere right now. Want to add that in the future but didn't have time.
 */
public class KartHunter{

    /**
     * Creates blank decks
     */
    public static final List<List<Card>> decks = new ArrayList<>();
    private static boolean exit = false;
    public static Scanner console = new Scanner(System.in);
    public static Card card = null;

    /**
     * main method for the program
     * @param args arguments
     * @throws IOException exception if error occurs while running the program.
     */
    public static void main(String[] args) throws IOException {
        initilizeAPI();
        initializeDecks();
        runMenu();
        console.close();
        exit();
    }

    /**
     * Allows the user to exit the program, by default the program will hang, inspecting the threads shows client stays connected to the MTG api, eventually closes, this simply speeds the process up.
     */
    private static void exit(){
        if(exit){
            System.exit(0);
        }
    }

    /**
     * Initializes MTGAPI to certain timeout settings, this was what was shown on the JavaSDK, so I used the same values. Doesn't seem to really have that much of an effect though unless the servers are running especially slow.
     */
    private static void initilizeAPI(){
        MTGAPI.setConnectTimeout(60);
        MTGAPI.setReadTimeout(60);
        MTGAPI.setWriteTimeout(60);
    }

    /**
     * fetches the cards using a filter, fastest implementation I could find for now.
     * @param filter A list of filters to enforce on the card search.
     * @return List of cards pertaining to the users search. The cards are fetched using a filter and the CardAPI, this seems to remain the fastest way to implement searching.
     */
    static List<Card> fetchCards(List<String> filter) {
        try {
            return CardAPI.getAllCards(filter);
        } catch (NullPointerException npe) {
            System.out.println("Error connecting to the server, please try again later.");
            return null;
        }
    }

    /**
     * Searches for cards by name and allows the user to select a card from the results, in future want to add ability for user to select multiple cards, and maybe even compare cards side by side
     */
    private static void searchCardsByName(){
        System.out.println("\n[ 0 ] EXIT");
        List<Card> cards;
        String name = getStringInput();
        if(!name.equals("0")){
            List<String> filter = new ArrayList<>(Collections.singleton("name=" + name));
            cards = fetchCards(filter);
            assert cards != null;
            if(!cards.isEmpty()){
                searchCardListPrint(cards);
                selectSearchCard(cards);
            }
            if(cards.isEmpty()) System.out.println("\n~Card could not be found~");
            searchCardsByName();
        }
    }

    /**
     * Displays a menu for the user to select a card from the quick search and perform various actions on it
     * @param cardSelect the list of cards for the user to select from
     * @param input user input that tells the method which card to select from
     */
    private static void selectSearchCardMenu(List<Card> cardSelect, int input){
        selectSearchCardMenuHeader();
        System.out.println();
        int choice = getInput(0,2);
        switch(choice){
            case 0:
                return;
            case 1:
                addCardToDeck(cardSelect.get(input - 1));
                break;
            case 2:
                card = cardSelect.get(input - 1);
                break;
            default:
                System.out.println("An error has occurred");
        }
    }

    /**
     * Allows the user to select a card in the search menu if they are unsure of whether they want the card, they can still add other cards and this one will stay selected
     * if a user wants they can go into the advanced search menu and do a detailed inspection on the card, or keep it selected and add it once they have viewed their decks.
     * In future want to add functionality to select multiple cards
     * <a href="https://stackoverflow.com/questions/34112276/java-format-string-spacing">...</a>
     * <a href="https://www.geeksforgeeks.org/java-string-format-method-with-examples/">...</a>
     * @param cardSelect the list of cards generated from a users search.
     */
    private static void selectSearchCard(List<Card> cardSelect){
        if(!cardSelect.isEmpty()) System.out.print("\n[ 1-"+cardSelect.size()+" ] Inspect card");
        if(card != null)System.out.print("     SELECTED CARD [" +card.getName() + "]");
        System.out.println("\n[ 0 ] EXIT\n");
        viewCardsInDeck(cardSelect);
        System.out.println();
        int input = getInput(0, cardSelect.size());
        if(input != 0){
            printCardInfo(cardSelect.get(input - 1));
            selectSearchCardMenu(cardSelect, input);
            selectSearchCard(cardSelect);
        }
    }

    /**
     * Uses a map with String names as keys, and cards as values, Did this because when searching for a card such as a forest, you would have multiple reprints of the same card
     * Another issue I ran into was multiverseIDs, on the API side the default multiverseID seems to be -1, so storing cards by multiverse ID wouldn't work because we have lots of repeats of those even amongst different cards
     * Using the names as keys allows a user to view unique cards only, this comes at the cost of not being able to view reprints, but for now this works. Test it out by searching for just "druid"
     * At the end this method takes the map and converts it pack to a list.
     * @param cards the List of cards generated from a users search
     */
    private static void searchCardListPrint(List<Card> cards){
        Map<String, Card> searchMap = new HashMap<>();
        for (Card value : cards) {
            String name = value.getName();
            searchMap.put(name, value);
        }
        if(!searchMap.isEmpty()){
            cards.clear();
            cards.addAll(searchMap.values());
        }
    }

    /**
     * Allows user to perform various actions in the search menu, uses recursion to stay until user wants to leave.
     */
    private static void searchCardMenu(){
        searchCardMenuHeader();
        switch(getInput(0,2)){
            case 0:
                break;
            case 1:
                addCardToDeck(card);
                searchCardMenu();
                break;
            case 2:
                searchCardsByName();
                searchCardMenu();
                break;
            default:
                System.out.println("Invalid input");
                break;
        }
    }

    /**
     * Initializes decks for the user, in future want to add ability for user to create and delete decks as well as have these decks load and save to local files.
     */
    public static void initializeDecks(){
        for (int i = 0; i < 5; i++){
            decks.add(new ArrayList<>());
        }
    }

    /**
     * prints a preview of the users decks for them to view.
     */
    private static void printDeckPreview(){
        for(int i = 0; i < decks.size(); i++){
            System.out.println("\n  Deck [" +(i + 1) + "]");
            viewCardsInDeck(decks.get(i));
        }
    }
    /**
     * The insertion point for our deck manager menu.
     */
    private static void Decks(){
        DecksMenuHeader();
        System.out.println("\n");
        printDeckPreview();
        int deckNum = getInput(0,decks.size());
        if(deckNum != 0) {
            editDeck(decks.get(deckNum - 1));
            System.out.println();
            Decks();
        }
    }

    /**
     * Allows a user to view the cards in their deck
     * @param deck the deck the user wants to view
     */
    private static void viewCardsInDeck(List<Card> deck){
        if(!deck.isEmpty()){
           deck.sort(Comparator.comparing(Card::getType));
           for(int i = 0; i < deck.size(); i++){
               String temp = String.format("Card [ %d ] %s",(i + 1), deck.get(i).getName());
               temp = String.format("%-50s [ Type ] %s", temp, deck.get(i).getType()); //done for nice spacing in quick search
               System.out.println(temp);
           }
        }
        if(deck.isEmpty()) System.out.println("~Empty deck~");
        System.out.println();
    }

    /**
     * Allows a user to edit their deck, uses the deckController as a helper
     * @param deck the deck the user wants to edit.
     */
    private static void editDeck(List<Card> deck){
        System.out.println("\n[1] Erase deck\n[2] Copy deck\n[3] Add card\n[4] Remove card\n[5] Inspect cards\n[0] EXIT\n");
        viewCardsInDeck(deck);
        int choice = getInput(0,5);
        if(choice != 0){
            deckController(deck,choice);
            editDeck(deck);
        }
    }

    /**
     * @param deck the deck we want to perform actions on
     * @param choice the integer of the action the user wants to perform
     */
    private static void deckController(List<Card> deck, int choice){
       switch(choice){
           case 1:
               deckErase(deck);
               return;
           case 2:
               copyDeck(deck);
               break;
           case 3:
               if(card != null){
                   deck.add(card);
                   System.out.println(card.getName() + " added");
                   return;
               }
               if(card == null)System.out.println("No card selected");
               break;
           case 4:
                removeCardFromDeck(deck);
                break;
           case 5:
               selectSearchCard(deck);
               break;
           default:
               System.out.println("An error has occurred");
               break;
       }
    }

    /**
     * Copies an existing deck to another deck that the user can specify
     * <a href="https://stackoverflow.com/questions/14319732/how-to-copy-a-java-util-list-into-another-java-util-list">...</a>
     * @param deck the deck that will be copied to deck of user choice
     */
     private static void copyDeck(List<Card> deck){
        System.out.println("<------------------------------------------>");
        System.out.println("[ 1-"+decks.size()+" ] Select output for copy\n[0] EXIT");
        printDeckPreview();
        int choice = getInput(0,decks.size());
        if(choice != 0){
            System.out.println("Copied to deck " + choice);
            decks.get(choice - 1).addAll(deck);
        }
     }

    /**
     * Allows a user to erase a deck if they want, won't delete the deck itself but will clear its contents if a user desires.
     * @param deck the deck we want to erase
     */
    private static void deckErase(List<Card> deck){
            System.out.println("\nConfirm erase\n[1] Yes\n[0] No\n");
            int choice = getInput(0, 1);
            if (choice == 1) {
                deck.clear();
                System.out.println("~DECK CLEARED~\n");
                return;
            }
            System.out.println("No changes made");
            viewCardsInDeck(deck);
    }

    /**
     * Adds card to the deck the user specifies
     * @param card the card we want to add to the deck
     */
     public static void addCardToDeck(Card card){
        if(card != null){
            System.out.println("\nWhat deck would you like to add [" + card.getName()+ "] to [ 1-" + decks.size() + " ] ");
            int deckNumber = getInput(1, 5);
            decks.get(deckNumber - 1).add(card);
            System.out.println("\n"+card.getName()+" added to deck " + deckNumber);
        }else{
            System.out.println("~Cannot add card, no card selected~");
        }
    }

    /**
     * Allows users to remove cards from their decks. Informs user of card they removed.
     * @param deck the deck we want to remove the specified card from
     */
    public static void removeCardFromDeck(List<Card> deck){
        System.out.println("\nEnter # card to remove\n[0] EXIT\n");
        int choice = getInput(0,deck.size());
        if(choice != 0) {
            System.out.println("REMOVED " + deck.get(choice - 1).getName());
            deck.remove(choice - 1);
            removeCardFromDeck(deck);
        }
    }

    /**
     * run menu function calls the printHeader and printMenu function, and getInput function.
     * @throws IOException when we call performActions the getInput has a chance to throw an IOException if user gives bad input, handled in that method though
     */
    private static void runMenu() throws IOException {
        while(!exit){
            printHeader();
            printMenu();
          performActions(getInput(0,3));
        }
    }

    /**
     * performs actions based on user input of specified integer, used for menu method.
     * @param choice integer value from user which will specify the action to be performed
     */
    private static void performActions(int choice){
        switch(choice){
            case 0:
                System.out.println("\nClosing KartHunter :)");
                exit = true;
                break;
            case 1:
                searchCardMenu();
                break;
            case 2:
                advancedSearch();
                break;
            case 3:
                Decks();
                break;
            default:
                System.out.println("An Error has occurred");
                break;
        }
    }

    /**
     * Allows users to access different functions to interact with the sets in magic, they can also use the detailedInspect menu to further inspect information about a card if they have one selected.
     * In the future I want to add the ability for a user to create a deck from a set, and select cards from the set, but for now this just allows the user to view all the sets, get a set code to search the sets and inspect selected cards from search menu.
     */
    private static void advancedSearch(){
            advancedSearchMenuHeader();
            int choice = getInput(0, 3);
            switch (choice) {
                case 1:
                    viewAllSets();
                    return;
                case 2:
                    searchSets();
                    return;
                case 3:
                    System.out.println("\n<Detailed Inspector Menu>\n[1] Card ID\n[2] Names\n[3] Mana\n[4] Colors\n[5] Color identity\n[6] Type\n[7] Text\n[8] Original text\n[9] Artist\n[10] Release date\n[11] Set name\n[0] EXIT\n");
                    detailedInspect();
                    return;
                case 0:
                    return;
                default:
                    System.out.println("Invalid input");
                    break;
            }
    }

    /**
     * Prints the cards in order as it appears in the set. Not entirely sure if I am using the stream api to max efficiency, but in testing this seems to load the sets in faster.
     * <a href="https://stackoverflow.com/questions/32797579/foreach-vs-foreachordered-in-java-8-stream">...</a>
     * <a href="https://www.youtube.com/watch?v=Q93JsQ8vcwY">...</a>
     */
    private static void searchSets(){
        MtgSet setList;
        System.out.print("\n[ Enter Set Code ]\n[ 0 ] BACK\n");
        String setCode = getStringInput();
        if(!setCode.equals("0")){
            try {
               setList = SetAPI.getSet(setCode);
                ((MtgSet) setList).getCards().parallelStream().forEachOrdered(KartHunter::printCardInfo);
                searchSets();
            } catch (NullPointerException npe){
                System.out.println("Set with that name does not exist");
            }
        }else if(setCode.equals("0")) advancedSearch();
    }

    /**
     * This method creates a list of sets using the set API, it then goes through the list and for each set in the list, prints the name of set, the set code, so you can search and view the cards in a particular set, the type the set is, as well as the release date
     * Formatted this using java format as it is way cleaner looking than adding those spaces manually. Looks pretty nice but in the future I'd want to make it, so it can sort by release date as that is how most other sites display all their sets.
     */
    private static void viewAllSets(){
        List<MtgSet> mtgSetList = SetAPI.getAllSets();
        for(MtgSet mtgSet : mtgSetList){
            System.out.printf("\nSET NAME: %-50s   SET CODE: %-20s    SET TYPE: %-15s    RELEASE DATE: %-10s", mtgSet.getName(), mtgSet.getCode(), mtgSet.getType().toUpperCase(), mtgSet.getReleaseDate());
        }
        System.out.println();
        advancedSearch();
    }

    /**
     *prints the header for the program, didn't have time to create a functioning GUI but plan is to replace all these headers with a GUI in the future
     */
    private static void printHeader(){
        System.out.println("+---------------------+");
        System.out.println("|Welcome to KartHunter|");
        System.out.println("|   ~ Main Menu ~     |");
        System.out.println("+---------------------+");
    }

    /**
     * prints the menu for our program
     */
    private static void printMenu(){
        System.out.println("\n<MAIN MENU>");
        System.out.println("[ 1 ] - Search Menu");
        System.out.println("[ 2 ] - Advanced Search Menu");
        System.out.println("[ 3 ] - Deck manager");
        System.out.println("[ 0 ] - EXIT PROGRAM\n");
    }
    /**
     * Header for the search card menu, made this so my methods above would not become overcrowded with print statements. if a user has a card selected it will display the selected card when in the menu
     */
    private static void searchCardMenuHeader(){
        System.out.println("\n<SEARCH MENU>");
        System.out.print("[ 1 ] - Add card to deck");
        if(card != null)System.out.print("           SELECTED CARD - [" +card.getName() + "]");
        System.out.println("\n[ 2 ] - Search");
        System.out.println("[ 0 ] - Main menu\n");
    }

    /**
     * Header for the advanced search menu, made this so my methods above would not become overcrowded with print statements.
     */
    private static void advancedSearchMenuHeader(){
        System.out.println("\n<Advanced Search Menu>");
        System.out.println("[ 1 ] - View all sets");
        System.out.println("[ 2 ] - Search set");
        System.out.println("[ 3 ] - Detailed Inspection");
        System.out.println("[ 0 ] - Main menu\n");
    }

    /**
     * Menu header for our deck menu, displays total number of decks to select from. In the future when I add the ability for the user to create decks this will account for that an update the total deck count
     */
    private static void DecksMenuHeader(){
        System.out.println("\n<Deck menu>");
        if(decks.isEmpty()) System.out.println("[ 1 ] Create deck\n[ 0 ] Main menu");
        System.out.print("[ 1-" + (decks.size()) + " ] View deck\n[ 0 ] EXIT");
    }

    /**
     * Simple menu header when selecting a card in the quick search menu, made this so my methods above wouldn't get too crowded with println statements
     */
    private static void selectSearchCardMenuHeader(){
        System.out.println("\n[ 1 ] Add card to deck");
        System.out.println("[ 2 ] Select card");
        System.out.println("[ 0 ] EXIT");
    }

    /**
     * Given a card, prints the info of the card if the card has that information available, some cards do not have certain info. Lands for instance do not have power/toughness so this method will omit that if the value is null
     * @param card the card we want to print the info of
     */
    private static void printCardInfo(Card card){
        if(card != null){
            System.out.print("_______________________");
            System.out.print("\n"+card.getName() + " ");
            if(card.getManaCost() != null) System.out.println(card.getManaCost());
            System.out.println(card.getType());
            if(card.getPower() != null && card.getToughness() != null) System.out.print("Power/Toughness: " + "["+card.getPower() + "/" + card.getToughness()+"]\n"); //sorta redundant to check for both because no cards in magic have just power or just toughness
            System.out.print(card.getText()+"\n");
        }
    }

    /**
     * getInput method fetches user input using a scanner
     * <a href="https://www.youtube.com/watch?v=25kUc_ammbw">...</a>
     * @return int choice, the choice for menu
     */
    private static int getInput(int min, int max){
        int choice = -1;
        while(choice < min || choice > max){
            try{
                System.out.print("Enter choice: ");
                choice = Integer.parseInt(KartHunter.console.nextLine().trim());
                if(choice < min || choice > max){
                    System.out.println("~Invalid selection. Please try again.~");
                }
            }catch(NumberFormatException nfe){
                System.out.println("\n~Invalid selection. Please try again.~");
            }
        }
        return choice;
    }

    /**
     * gets string input from the user, trims it and converts to lower case.
     * @return String trimmed input from the user converted to lower case.
     */
    private static String getStringInput(){
        String str;
        System.out.print("Enter search: ");
        str = console.nextLine();
        if(str.isEmpty()){
            System.out.println("Input cannot be blank");
        }
        return str.trim().toLowerCase();
    }

    /**
     *Allows user to find out more about their selected card. Stuff like...
     * MultiverseId - probably not something a user would really care about but included anyway
     * Names - variations of names, if a card is multi sided it will give the names of the other side, beyond that does nothing
     * Mana - fetches mana cost of card
     * Colors - fetches the mana color of the card
     * Color identity - The combination of all colors in its mana cost, any color indicator or color-setting characteristic-defining abilities on the card, and any mana symbols in the card's rules text.
     * Type - artifact, battle, conspiracy, creature, dungeon, enchantment, instant, land, phenomenon, plane, planeswalker, scheme, sorcery, tribal, and vanguard.
     * Text - The text box is printed on the lower half of the card. It usually contains rules text defining the card's abilities.
     * Original Text - If card is a reprint and has a different text, this will print the original text that the card released with.
     * Artist - Displays the name of the artist who created the card, pretty useless right now because user can't select from different prints..
     * Release date - Displays the release date of the card.
     * Set name - Displays the name of the set the card is contained in, as well as the set code if the user wants to search for that cards set in the set search menu
     */
    public static void detailedInspect() {
        while(true){
            if (card == null){
                System.out.println("\n~No card selected, please select a card~");
                advancedSearch();
                return;
            }else{
                int choice = getInput(0, 11);
                switch (choice) {
                    case 1:
                        if(card != null)System.out.println(card.getMultiverseid());
                        else System.out.println("Information not available for this card.");
                        break;
                    case 2:
                        if(card.getNames() != null)System.out.println(Arrays.toString(card.getNames()));
                        else System.out.println("Information not available for this card.");
                        break;
                    case 3:
                        if(card.getManaCost() != null)System.out.println(card.getManaCost());
                        else System.out.println("Information not available for this card.");
                        break;
                    case 4:
                        if(card.getColors() != null)System.out.println(Arrays.toString(card.getColors()));
                        else System.out.println("Information not available for this card.");
                        break;
                    case 5:
                        if(card.getColorIdentity() != null)System.out.println(Arrays.toString(card.getColorIdentity()));
                        else System.out.println("Information not available for this card.");
                        break;
                    case 6:
                        if(card.getType() != null)System.out.println(card.getType());
                        else System.out.println("Information not available for this card.");
                        break;
                    case 7:
                        if(card.getText() != null)System.out.println(card.getText());
                        else System.out.println("Information not available for this card.");
                        break;
                    case 8:
                        if(card.getOriginalText() != null)System.out.println(card.getOriginalText());
                        else System.out.println("Information not available for this card.");
                        break;
                    case 9:
                        if(card.getArtist() != null)System.out.println(card.getArtist());
                        else System.out.println("Information not available for this card.");
                        break;
                    case 10:
                        if(card.getReleaseDate() != null)System.out.println(card.getReleaseDate());
                        else System.out.println("Information not available for this card.");
                        break;
                    case 11:
                        if(card.getSetName() != null)System.out.println(card.getSetName() +"     Set code: "+card.getSet());
                        else System.out.println("Information not available for this card.");
                        break;
                    case 0:
                        advancedSearch();
                        return;
                    default:
                        throw new IllegalStateException("Unexpected value: " + choice);
                }
            }
        }
    }
}
