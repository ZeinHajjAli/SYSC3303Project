public class Start
{

    //main class that should be run
    public static void main(String[] args) {

        //initializes the table, the three different chefs, and the agent
        Table table = new Table();
        Thread jamChef = new Chef("jameChef", table, "jam");
        Thread breadChef = new Chef("breadChef", table, "bread");
        Thread peanutButterChef = new Chef("peanutButterChef", table, "peanutButter");
        Thread agent = new Agent("agent", table);

        //starts the chefs and the agent threads
        agent.start();
        jamChef.start();
        breadChef.start();
        peanutButterChef.start();

    }
}
