import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.util.Set;
import java.util.List;
import java.util.HashSet;

public class QueryGenerator{
    private static int V;
    private static List<Vertex> vertices;
    private static Set<Query> setOfQueries;

    public static void main(String[] a){
        //a[0] - query type (0=local query, 1=random query)
        //a[1] - number of query pairs
        //a[2] - radius (in meters)
        //a[3] - node coordinate (.co) file

        Scanner sc=null;
        vertices=new ArrayList<Vertex>();
        setOfQueries=new HashSet<Query>();
        int queryType=0;
        int noOfQueryPairs=0;
        int radius=0;
        File queryPairs=null;
        File nodeCoordFile=null;

        try{
            queryType=Integer.parseInt(a[0]);
            noOfQueryPairs=Integer.parseInt(a[1]);
            radius=Integer.parseInt(a[2]);
            nodeCoordFile=new File(a[3]);

            if (nodeCoordFile.exists())
                sc = new Scanner(nodeCoordFile);

        }catch (IOException ex) {
            throw new IllegalArgumentException("Could not open file");
        }

        //load nodes
        addNodes(sc);

        switch(queryType){
            case 0:{
                //randomly select a source vertex
                Random rand=new Random();
                int randSrcVertexId=rand.nextInt(V)+1;
                Vertex s=vertices.get(randSrcVertexId); //source vertex
                int queryId=0;
                String nodeCoordFileName=nodeCoordFile.getName();
                String fileName=nodeCoordFileName.substring(0, nodeCoordFileName.indexOf('.'))+"_queries.loc.p2p";
                queryPairs=new File(".//input//"+fileName);

                while(setOfQueries.size()<noOfQueryPairs){
                    int randDestVertexId=rand.nextInt(V) +1;
                    Vertex d=vertices.get(randDestVertexId); //potential destination vertex
                    if(s != null && d != null && withinRadius(s, d, radius)){
                        Query q=new Query(queryId++, s.getId(), d.getId());
                        setOfQueries.add(q);
                    }
                }

                break;
            }
            case 1:{
                //randomly select two vertices
                Random rand=new Random();
                int srcVertexId=rand.nextInt(V)+1;
                int destVertexId=rand.nextInt(V)+1;
                String nodeCoordFileName=nodeCoordFile.getName();
                String fileName=nodeCoordFileName.substring(0, nodeCoordFileName.indexOf('.'))+"_queries.rand.p2p";
                queryPairs=new File(".//input//"+fileName);

                for(int i=0;i<noOfQueryPairs;i++){
                    Query q=new Query(i,srcVertexId, destVertexId);
                    setOfQueries.add(q);

                    //generate new randoms
                    srcVertexId=rand.nextInt(V)+1;
                    destVertexId=rand.nextInt(V)+1;
                }
                break;
            }
        } //end switch


        //write to file
        writeToFile(queryPairs, setOfQueries);

    }


    private static void writeToFile(File file, Set<Query> setOfQueries){

        if(file != null){
            try{
                //create new file
                file.createNewFile();
                FileWriter writer = new FileWriter(file);

                writer.write("c define the problem in terms of number of queries\n");
                writer.write("c problem specification file contains " + setOfQueries.size()+" query pairs\n");
                writer.write("p aux sp p2p " + setOfQueries.size()+"\n");
                writer.write("c\n");
                writer.write("c");

                for(Query q: setOfQueries){
                    writer.write("\nq " + q.getSrcVertex() + " " + q.getDestVertex());
                }

                writer.flush();
                writer.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }


    private static boolean withinRadius(Vertex s, Vertex t, int radius){
        if(dist(s,t) <= radius)
            return true;
        return false;
    }

    //distance calculation code was taken from http://www.geodatasource.com/developers/java
    //distance is returned in unit used by DIMACS
    private static int dist(Vertex a, Vertex b){

        //convert lon/lat into x/y coordinate system
        double latA=(double)a.getLatitude() * 0.000001;
        double lonA=(double)a.getLongitude() * 0.000001;

        double latB=(double)b.getLatitude() * 0.000001;
        double lonB=(double)b.getLongitude() * 0.000001;

        double theta = lonA - lonB;
        double dist = Math.sin(deg2rad(latA)) * Math.sin(deg2rad(latB)) +
                Math.cos(deg2rad(latA)) * Math.cos(deg2rad(latB)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344;  //in kilometers
        dist=dist*1000; //in meters

        return (int)dist;
    }

    //this function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    //this function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private static void addNodes(Scanner in){
        //read first byte in line
        char c=in.next().charAt(0);

        //add dummy vertex with id = 0 to the list
        vertices.add(0, null);

        //iterate through all nodes and add lon/lat data
        while(in.hasNextLine()){
            switch (c){
                case 'c': {
                    //skip the line
                    in.nextLine();

                    //read the byte in the next line
                    if (in.hasNext())
                        c = in.next().charAt(0);

                    break;
                }

                case 'p': {
                    //read the definition of a problem
                    in.next(); //read the word 'aux'
                    in.next(); //read the word 'sp'
                    in.next(); //read the word 'co'
                    int noOfVertices = in.nextInt(); //get the number of vertices
                    if (noOfVertices < 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");
                    V = noOfVertices;

                    //skip to the next line
                    in.nextLine();

                    //read the byte in the next line
                    if (in.hasNext())
                        c = in.next().charAt(0);

                    break;
                }

                case 'v': {
                    //read the node information
                    int nodeId = in.nextInt();
                    validateVertex(nodeId);

                    int lon = in.nextInt();
                    int lat = in.nextInt();
                    validateLongitude(lon);
                    validateLatitude(lat);

                    Vertex v=new Vertex(nodeId,""+nodeId,lon,lat);
                    vertices.add(nodeId, v);

                    //read the byte in the next line
                    if (in.hasNext())
                        c = in.next().charAt(0);

                    break;
                }
            }//end switch
        }
    }

    //throw an exception unless 1 <= vertexId <= V
    private static void validateVertex(int vertexId) {
        if (vertexId < 1 || vertexId > V)
            throw new IndexOutOfBoundsException("vertex " + vertexId + " is not between 1 and " + V);
    }

    //throw an exception unless 180.000000 W <= longitude <= 180.000000 E
    private static void validateLongitude(int longitude){
        if(longitude < -180000000 || longitude > 180000000)
            throw new IndexOutOfBoundsException("longitude " + longitude + " is not between 180 deg West and 180 deg East");
    }

    //throw an exception unless 90.000000 S <= latitude <= 90.000000 N
    private static void validateLatitude(int latitude){
        if(latitude < -90000000 || latitude > 90000000)
            throw new IndexOutOfBoundsException("latitude " + latitude + " is not between 90 deg South and 90 deg North");
    }
}
