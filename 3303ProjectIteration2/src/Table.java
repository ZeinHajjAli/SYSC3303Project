public class Table
{
    //variables for each ingrediant
    private boolean jam;
    private boolean bread;
    private boolean peanutButter;

    //constructs the table as empty of all ingrediants
    public Table(){
        jam = false;
        bread = false;
        peanutButter = false;
    }

    //synchronized place method
    public synchronized void place(String item)
    {

        //send the thread to a wait state if it is already on the table
        if(item.equals("jam")){
            while(jam){
                try {
                    //sends thread to wait state
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            jam = true;
            System.out.println("added Jam");
            //notify all threads waiting on this method
            notifyAll();
        }
        //send the thread to a wait state if it is already on the table
        else if(item.equals("bread")) {
            while (bread) {
                try {
                    //sends thread to wait state
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            bread = true;
            System.out.println("added bread");
            //notify all threads waiting on this method
            notifyAll();
        }
        //send the thread to a wait state if it is already on the table
        else if(item.equals("peanutButter")) {
            while (peanutButter) {
                try {
                    //sends thread to wait state
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            peanutButter = true;
            System.out.println("added pb");
            //notify all threads waiting on this method
            notifyAll();
        }

    }

    //synchronized get method
    public synchronized boolean get(String item1, String item2){
        System.out.println("triggered get " + item1 + " " + item2);
        if(item1.equals("jam")&&item2.equals("bread")){
            //if either are not available, sends to wait state
            while(!jam||!bread){
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            jam = false;
            bread = false;
            //notify all threads in wait state for this method
            notifyAll();
            return true;
        }
        else if(item1.equals("jam")&&item2.equals("peanutButter")){
            //if either are not available, sends to wait state
            while(!jam||!peanutButter){
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            jam = false;
            peanutButter = false;
            //notify all threads in wait state for this method
            notifyAll();
            return true;
        }
        else if(item1.equals("bread")&&item2.equals("peanutButter")){
            //if either are not available, sends to wait state
            while(!bread||!peanutButter){
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            bread = false;
            peanutButter = false;
            //notify all threads in wait state for this method
            notifyAll();
            return true;
        }
        else{
            return false;
        }
    }

    //synchronized check if table is empty
    public synchronized boolean isEmpty(){
        boolean firstTwo = jam||bread;
        boolean theThird = firstTwo||peanutButter;
        System.out.println(!theThird);
        //if it isnt empty,  sends to wait state
        while(theThird){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //notifys all threads in wait state for this method
        notifyAll();
        return true;

    }
}
