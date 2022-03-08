import java.io.*;
import java.util.Scanner;
import java.time.LocalDateTime;

public class Blackjack {

    // Werte der jeweiligen Hand
    public static int playerValue = 0;

    public static int dealerValuePublic = 0;

    public static int dealerValuePrivate = 0;

    public static char answer;

    public static String dealerCardTwo;

    public static String winner;

    public static int playerCounterAce = 0;

    public static int dealerCounterAce = 0;

    public static int balance = 0;

    public static int betAmount = 0;

    public static void main(String[] args) {

        // Wert der Hand des Spielers wird durch die Methode playerValueAllocation bestimmt
        readerBalance();

        betMoney();

        playerValue = playerValueAllocation();
        dealerValuePrivate = dealerValueAllocation();

        answer = stayOrHit();

        playerValue = drawNewCardCheck();

        dealerValuePrivate = dealerNewCardDraw(dealerCardTwo);

        winner();

        updateBalance();

        writer();

        reader();
    }

    public static void updateBalance() {

        String balanceAsString = String.valueOf(balance);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("blackjackstats/balance.txt"))) {

            writer.write(balanceAsString);
            writer.newLine();
        }
        catch (IOException e) {

            System.out.println("Ein Fehler beim Beschreiben der Datei ist aufgetreten: " + e.getMessage());
        }

        System.out.println("\n" + "Sie haben nun einen Kontostand von: " + balance + "€");
    }

    public static void readerBalance() {

        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader("blackjackstats/balance.txt"))) {

            while ((line = reader.readLine()) != null) {

                System.out.println("Sie haben einen Kontostand von: " + line + "€");

                balance = Integer.parseInt(line);
            }
        }
        catch (IOException ex) {

            System.out.println("Ein Fehler beim Lesen der Datei ist aufgetreten: " + ex.getMessage());
        }

        if (balance <= 0) {

            System.out.println("Wie viel Geld moechten Sie aufladen?");

            balance = input.nextInt();

            System.out.println("Sie haben nun einen Kontostand von: " + balance + "€");
        }
    }

    public static void betMoney() {

        System.out.println("Wie viel Geld möchten Sie setzen?");

        betAmount = input.nextInt();

        if (betAmount <= balance) {

            balance -= betAmount;
        }
        else {

            System.out.println("Die eingegebene Summe ueberschreitet Ihren Kontostand");
        }

        System.out.println();
    }

    public static void writer() {

        LocalDateTime localDateTime = LocalDateTime.now();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("blackjackstats/history.txt", true))) {

            String[] textToWrite = {

                    "Historie für den " + localDateTime + " ist Spieler: " + playerValue + " und fuer den Dealer: " +
                            dealerValuePrivate + " (Sieger: " + winner + ")"
            };
            for (String line : textToWrite) {

                writer.write(line);
                writer.newLine();
            }
        }
        catch (IOException e) {

            System.out.println("Ein Fehler beim Beschreiben der Datei ist aufgetreten: " + e.getMessage());
        }
    }

    public static void reader() {

        System.out.println("\n" + "Falls Sie Ihre Spielhistorie ausgeben möchten, geben Sie bitte 'J' ein");

        char blackjackstats = input.next().charAt(0);

        if (blackjackstats == 'J') {

            String line;

            try (BufferedReader reader = new BufferedReader(new FileReader("blackjackstats/history.txt"))) {

                while ((line = reader.readLine()) != null) {

                    System.out.println(line);
                }
            }
            catch (IOException ex) {

                System.out.println("Ein Fehler beim Lesen der Datei ist aufgetreten: " + ex.getMessage());
            }
        }
    }

    // Wird genutzt um Input zu registrieren und um in Methoden verwendet zu werden
    public static Scanner input = new Scanner(System.in);

    // Methode um den Wert der Hand des Spielers zu berechnen
    public static int playerValueAllocation () {

        System.out.println("Der Spieler zieht zwei Karten");

        // "Sichtbare" Karte wird gezogen
        String playerCardOne = drawCard(true);
        // Sichtbare Karte wird zur Methode cardValueAllocation übergeben um zu überprüfen, welchen Wert die Karte hat
        playerValue += cardValueAllocation(playerCardOne, playerValue);

        String playerCardTwo = drawCard(true);
        playerValue += cardValueAllocation(playerCardTwo, playerValue);

        System.out.println("Der Spieler hat den Wert " + playerValue + "\n");

        return playerValue;
    }

    public static char stayOrHit () {

        System.out.println("\n" + "S(tay) or H(it)?");

        char continuePlayer = input.next().charAt(0);

        System.out.println();

        return continuePlayer;
    }

    public static int drawNewCard (int value, boolean isPlayerPlayer) {

        int newValue = value;

        String newCard = drawCard(true);

        if (isPlayerPlayer) {

            newValue += cardValueAllocation(newCard, newValue);
        }
        else {

            newValue += cardValueAllocationDealer(newCard, newValue);
        }

        System.out.println("Der neue Wert ist " + newValue);

        return newValue;
    }

    public static int drawNewCardCheck () {

        while (answer == 'H' && playerValue <= 21) {

            playerValue = drawNewCard(playerValue, true);

            while (playerCounterAce > 0 && playerValue > 21) {

                playerValue -= 10;

                --playerCounterAce;

                System.out.println("Die Hand beträgt nun den Wert " + playerValue + ", da ein Ass zu einer 1 gemacht wurde");
            }

            if (playerValue < 21) {

                answer = stayOrHit();
            }
            else if (playerValue == 21) {

                System.out.println("Der Spieler hat den Wert 21 und kann daher keine weiteren Karten ziehen");
            }
            else {

                System.out.println("Der Spieler hat einen Wert von ueber 21, er hat verloren");
            }
        }

        return playerValue;
    }

    public static int dealerValueAllocation () {

        System.out.println("Der Dealer zieht zwei Karten");

        String dealerCardOne = drawCard(true);
        dealerValuePublic += cardValueAllocationDealer(dealerCardOne, dealerValuePublic);
        dealerValuePrivate += cardValueAllocationDealer(dealerCardOne, dealerValuePrivate);

        dealerCardTwo = drawCard(false);
        dealerValuePrivate += cardValueAllocationDealer(dealerCardTwo, dealerValuePrivate);

        System.out.println("Der Dealer hat den Wert " + dealerValuePublic + "\n");
        System.out.println("Test: Der Dealer hat den privaten Wert " + dealerValuePrivate);

        return dealerValuePrivate;
    }

    public static String drawCard (boolean visibility) {

        String[] cards = {"Ass", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Bube", "Dame", "Koenig"};

        // Zufallswert wird bestimmt -
        int randomCardValue = (int) (Math.random() * cards.length);

        // - um eine zufälle Karte aus dem Array zu schnappen
        String drawnCard = cards[randomCardValue];

        // Falls die Karte aufgedeckt erscheinen soll
        if (visibility) {

            if (drawnCard.equals("Ass")) {

                System.out.println("Ein Ass wurde gezogen");
            }
            else {

                System.out.println("Es wurde ein(e) " + drawnCard + " gezogen");
            }
        }
        // Dealer benötigt eine verdeckte Karte
        else {

            System.out.println("Es wurde eine verdeckte Karte gelegt");
        }

        return drawnCard;
    }

    public static int cardValueAllocation (String card, int playerValue) {

        int value;

        // Ass kann 1 oder 11 sein bei BlackJack
        if (card.equals("Ass") && playerValue < 11) {

            value = 11;

            System.out.println("Das Ass nimmt den Wert 11 an");

            playerCounterAce += 1;
        }
        // Entscheidung des Spielers nicht mehr benötigt, auf Grund der Natur von BlackJack
        else if (card.equals("Ass")) {

            value = 1;

            System.out.println("Die Hand des Spielers beträgt einen Wert von über 10, das Ass nimmt den Wert 1 an");
        }
        // Alle Karten von 10 bis König haben den selben Wert
        else if (card.equals("Bube") || card.equals("Dame") || card.equals("Koenig")) {

            value = 10;
        }
        // Auf Grund der Natur der If-Abfrage kann in diesem Fall jede andere Karte nur zwischen 2 und 9 sein
        else {

            // String wird in ein Integer-Wert umgewandelt, da die Strings sowieso nur Zahlen von 2 bis 9 sind
            value = Integer.parseInt(card);
        }

        return value;
    }

    public static int cardValueAllocationDealer (String card, int dealerValuePrivate) {

        int value;

        if (card.equals("Ass")) {

            if (dealerValuePrivate >= 11) {

                value = 1;
            }
            else {

                value = 11;

                dealerCounterAce += 1;
            }
        }
        else if (card.equals("10") || card.equals("Bube") || card.equals("Dame") || card.equals("Koenig")) {

            value = 10;
        }
        else {

            value = Integer.parseInt(card);
        }

        return value;
    }

    public static int dealerNewCardDraw(String dealerCardInvisible) {

        if (playerValue <= 21) {

            System.out.println("\n" + "Die verdeckte Karte des Dealers war " + dealerCardInvisible);

            System.out.println("Der Dealer hat den Wert " + dealerValuePrivate);

            while (dealerValuePrivate < 17) {

                dealerValuePrivate = drawNewCard(dealerValuePrivate, false);

                while (dealerCounterAce > 0 && dealerValuePrivate > 21) {

                    dealerValuePrivate -= 10;

                    --dealerCounterAce;

                    System.out.println("Die Hand beträgt nun den Wert " + dealerValuePrivate + ", da ein Ass zu einer 1 gemacht wurde");
                }

                if (dealerValuePrivate > 21) {

                    System.out.println("Der Dealer hat einen Wert von ueber 21, der Spieler hat somit gewonnen");
                }
                else if (dealerValuePrivate == 21){

                    System.out.println("Der Dealer hat den Wert 21 erreicht");
                }
                else {

                    System.out.println("Der Dealer hat nun den Wert " + dealerValuePrivate + "\n");
                }
            }
        }

        return dealerValuePrivate;
    }

    public static void winner() {

        if (playerValue > 21) {

            System.out.println("Der Dealer hat gewonnen");

            winner = "Dealer";

            betAmount = 0;
        }
        else if (dealerValuePrivate > 21) {

            System.out.println("Der Spieler hat gewonnen");

            winner = "Spieler";

            betAmount = (betAmount * 2);

            balance += betAmount;
        }
        else if (playerValue < dealerValuePrivate) {

            System.out.println("Der Dealer hat gewonnen");

            winner = "Dealer";

            betAmount = 0;
        }
        else if (playerValue > dealerValuePrivate) {

            System.out.println("Der Spieler hat gewonnen");

            winner = "Spieler";

            betAmount = (betAmount * 2);

            balance += betAmount;
        }
        else {

            System.out.println("Spieler sowie Dealer haben den selben Endwert, unentschieden");

            winner = "Unentschieden/Keiner hat gewonnen";

            balance += betAmount;
        }
    }
}
