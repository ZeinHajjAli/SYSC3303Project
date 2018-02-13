public class Chef extends Thread
{

    //initialize ingrediant variables
    private boolean jam = true;
    private boolean bread = true;
    private boolean peanutButter = true;
    //keeps record of infinite supply
    private String infinite;
    //reference to table created in start
    private Table table;
    //return variable for get method
    private boolean confirm;

    //Constructor
    public Chef(String name, Table table, String item) {

        //name of the thread
        super(name);

        //sets item variables to corrent values
        if (item.equals("jam")) {
            jam = true;
            bread = false;
            peanutButter = false;
            infinite = "jam";
        } else if (item.equals("bread")) {
            bread = true;
            jam = false;
            peanutButter = false;
            infinite = "bread";
        } else if (item.equals("peanutButter")) {
            peanutButter = true;
            jam = false;
            bread = false;
            infinite = "peanutButter";
        }

        this.table = table;
    }

    //method that executes when the thread is started
    public void run() {
        //runs only 4 times
        for(int j=0; j<4; j++) {
            if (jam) {

                //synchronized critical code so that there is no interference
                synchronized (currentThread()) {
                    //checks if ingrediants available on table
                    confirm = table.get("bread","peanutButter");
                    peanutButter = confirm;
                    bread = confirm;
                }
            }
            else if (bread) {
                //synchronized critical code so that there is no interference
                synchronized (currentThread()) {
                    //checks if ingrediants available on table
                    confirm = table.get("jam","peanutButter");
                    peanutButter = confirm;
                    jam = confirm;
                }
            }
            else if (peanutButter) {
                //synchronized critical code so that there is no interference
                synchronized (currentThread()) {
                    //checks if ingrediants available on table
                    confirm = table.get("jam","bread");
                    jam = confirm;
                    bread = confirm;
                }
            }


            //"makes sandwich + eats it" instead of having a sandwich variable that will be consumed instantly, I will simply set the ingerdiants to false to signify eating it
            if(jam && bread && peanutButter){
                jam = false;
                bread = false;
                peanutButter = false;
                System.out.println(currentThread().getName() + " ate a sandwich");

                refill();
            }
            else{
                refill();
            }



        }
    }

    //refills ingrediants and sets the rest to their correct values
    private void refill(){
        if (infinite.equals("jam")) {
            jam = true;
            bread = false;
            peanutButter = false;
        } else if (infinite.equals("bread")) {
            bread = true;
            jam = false;
            peanutButter = false;
        } else if (infinite.equals("peanutButter")) {
            peanutButter = true;
            jam = false;
            bread = false;
        }
    }

}
