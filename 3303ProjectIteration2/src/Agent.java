import java.util.Random;

//Agent places 2 ingrediants at random on the table at once (only 10 times)
public class Agent extends Thread
{


    private Random rand = new Random();
    private int num;
    //table reference to object created in Start
    private Table table;

    //constructor
    public Agent(String name, Table table)
    {
      //give the thread a name
        super(name);
        this.table = table;
    }

    public void run()
    {


        for(int j=0; j<10; j++) {
            //synchronized critical sections of code to execute without interruption
            synchronized (currentThread()) {

                //randomly select 2 ingrediants to place on table
                num = rand.nextInt(3);
                if (num == 0) {
                    table.place("jam");
                    num = rand.nextInt(2);
                    if(num==0){
                        table.place("bread");
                    } else if (num==1){
                        table.place("peanutButter");
                    }
                } else if (num == 1) {
                    table.place("bread");
                    num = rand.nextInt(2);
                    if(num==0){
                        table.place("jam");
                    } else if (num==1){
                        table.place("peanutButter");
                    }
                } else if (num == 2) {
                    table.place("peanutButter");
                    num = rand.nextInt(2);
                    if(num==0){
                        table.place("bread");
                    } else if (num==1){
                        table.place("jam");
                    }
                } else {
                }
                //notify user that ingrediants havce been placed on table
                System.out.println(currentThread().getName() + " added ingredients");
            }
        }
        //ends thread execution after 10 loops
        return;
    }

}
