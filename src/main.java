import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class main {
    public static void main(String[] args) throws IOException {
        String[] paths = {"//home//maork//Semester 5//Distributed Data Systems//Home Work//Distributed Data Systems Project//src//input_1.txt"}; //enter the path to the files you want to run here.
        for (String path : paths) {
            ExManager m = new ExManager(path);
            m.read_txt();

            int num_of_nodes = m.getNum_of_nodes();

            Scanner scanner = new Scanner(new File(path));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if(line.contains("start")){
                    m.start();
                    for(Node node: m.list_of_nodes){
                        System.out.println("Node number " + String.valueOf(node.get_node_id()) + " Matrix");
                        System.out.println();
                        node.print_graph();
                        System.out.println();

                    }
                    System.out.println();
                    System.out.println();
                }

                if (line.contains("update")) {
                    String[] data = line.split(" ");
                    m.update_edge(Integer.parseInt(data[1]), Integer.parseInt(data[2]), Double.parseDouble(data[3]));
                }
            }
        m.terminate();
        }
    }
}