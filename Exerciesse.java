import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;


import java.util.Properties;

public class Exerciesse {
    public static final String CONNECTION_STRING =
            "jdbc:mysql://localhost:3306/";
    public static final String DB_NAME = "minions_db";
    private static final BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
    private  static  Connection connection;

    public static void main(String[] args)throws IOException, SQLException {
    connection=getConnection();
        System.out.println("Enter ex num:");
        int exNum=Integer.parseInt(reader.readLine());

        switch (exNum){
            case 2:exTwo();
            case 3:exThree();
            case 4:exFour();
           case 5:exFive();
            case 7:exSeven();
            case 8:exEight();
            case 9:exNine();

        }


    }


    private static  void exTwo()throws SQLException{
        PreparedStatement preparedStatement= connection.prepareStatement(
                "select v.name,count(distinct  mv.minion_id) as `m_count` from villains v " +
                        "join  minions_villains mv on v.id=mv.villain_id " +
                        "group by  v.name " +
                        "having `m_count`>? ;"
        );

        preparedStatement.setInt(1,15);
        ResultSet resultSet=preparedStatement.executeQuery();
        while (resultSet.next()){
            System.out.printf("%s %d %n",resultSet.getString(1),resultSet.getInt(2));;
        }
    }

    private static  void exThree() throws SQLException, IOException {
        System.out.println("Enter villain id");
        int villainId=Integer.parseInt(reader.readLine());
        String villainName1=findVillainNameById(villainId);

        String villainName=findEntityNameById("villains",villainId);
        PreparedStatement preparedStatement=connection.prepareStatement(
                "select m.name,m.age from minions m " +
                        "join minions_villains mv on m.id = mv.minion_id " +
                        "where mv.villain_id=?;"
        );
        preparedStatement.setInt(1,villainId);
        ResultSet resultSet=preparedStatement.executeQuery();

        int counter=0;
        System.out.println(villainName);

        while (resultSet.next()){
            System.out.printf("%d. %s %d %n",
                    ++counter,
                    resultSet.getString("name"),
                    resultSet.getInt("age"));
        }
//posledniq exeption
    }

    private static void exFour() throws IOException, SQLException {
        System.out.print("Minion: ");
        String[] minionInfo= reader.readLine().split("\\s");
       String nameM= minionInfo[0];
       int ageM=Integer.parseInt(minionInfo[1]);
       String townM=minionInfo[2];
        System.out.println("Villain: ");
        String villain=reader.readLine();
//if()
        PreparedStatement preparedStatement= connection
                .prepareStatement("insert into villains(name,evilness_factor) " +
                        "select ?,'evil' " +
                        "where not exists(select `name` from villains where name=?);");
        preparedStatement.setString(1,villain);
        preparedStatement.setString(2,villain);
        preparedStatement.execute();

        PreparedStatement preparedStatement1=connection
                .prepareStatement("select name " +
                        "from villains " +
                        "where name=?;");
        preparedStatement1.setString(1,villain);
        ResultSet resultSet=preparedStatement1.executeQuery();
        while (resultSet.next()){
            System.out.printf("Villain %s was added to the database.",resultSet.getString("name"));
            System.out.println();
        }
//        PreparedStatement preparedStatement2=connection
//                .prepareStatement()

    }

    private static void exFive() throws IOException, SQLException {
        System.out.println("Enter country name:");
        String countryName= reader.readLine();

        PreparedStatement preparedStatement=connection.prepareStatement
                ("UPDATE towns set name=upper (name) where country=?");
        preparedStatement.setString(1,countryName);
        int affectedRows=preparedStatement.executeUpdate();
        if(affectedRows==0){
            System.out.println("No town names were affected.");
            return;
        }
        System.out.printf("%d town names were affected.%n",affectedRows);
        PreparedStatement preparedStatementTowns=connection
                .prepareStatement("SELECT  name from towns where country=?");
        preparedStatementTowns.setString(1,countryName);
        ResultSet resultSet=preparedStatementTowns.executeQuery();

        while (resultSet.next()){
            System.out.println(resultSet.getString("name"));
        }

    }

    private static void exSeven() throws SQLException {
        PreparedStatement preparedStatement=connection.prepareStatement("select name from minions");

        ResultSet resultSet=preparedStatement.executeQuery();

        List<String>allMinionsNames=new ArrayList<>();

        while (resultSet.next()){
            allMinionsNames.add(resultSet.getString(1));
        }

        int start=0;
        int end=allMinionsNames.size()-1;
        for (int i = 0; i < allMinionsNames.size(); i++) {
            System.out.println(i%2==0
                    ?allMinionsNames.get(start++)
                    :allMinionsNames.get(end--));
        }
    }

    private static void exEight() throws IOException, SQLException {
        System.out.println("Enter minion ids:");
        String[] arr=reader.readLine().split(" ");
        int[] intarr=new int[arr.length];

        for(int i=0;i<arr.length;i++) {
            intarr[i] = Integer.parseInt(arr[i]);
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "update minions " +
                            "set age=age+1 " +
                            "where id=?;"
            );
            preparedStatement.setInt(1, intarr[i]);
            preparedStatement.executeUpdate();
            System.out.println(intarr[i]);
        }

        PreparedStatement preparedStatement1=connection.prepareStatement("select lower(name)as `name`,age from minions ;\n");
        ResultSet resultSet=preparedStatement1.executeQuery();
        while (resultSet.next()){
            System.out.printf("%s %d%n",resultSet.getString("name"),resultSet.getInt("age"));
        }
    }
    
    private static void exNine() throws IOException, SQLException {
        System.out.println("Enter minion id :");
        int minionId=Integer.parseInt(reader.readLine());

        CallableStatement callableStatement=connection
                .prepareCall("CALL usp_get_older(?)");
        callableStatement.setInt(1,minionId);
        int affected=callableStatement.executeUpdate();
        System.out.println();
    }


    private static Set<String> getAllMinionsByVillainId(int villainId) throws SQLException {
        Set<String>result=new LinkedHashSet<>();
        PreparedStatement preparedStatement=connection.prepareStatement(
                "select m.name,m.age from minions m " +
                        "join minions_villains mv on m.id = mv.minion_id " +
                        "where mv.villain_id=?;"
        );
        preparedStatement.setInt(1,villainId);
        ResultSet resultSet=preparedStatement.executeQuery();

        int counter=0;

        while (resultSet.next()){
            result.add(String.format("%d. %s %d",++counter,
                    resultSet.getString("name"),
                    resultSet.getInt("age")
                    ));
        }
        return result;
    }

    private static String findEntityNameById(String tableName, int entityId) throws SQLException {
        String query=String.format("select name from %s where id=?",tableName);
        PreparedStatement preparedStatement=connection.prepareStatement(query);
        preparedStatement.setInt(1,entityId);
        ResultSet resultSet=preparedStatement.executeQuery();
        resultSet.next();
        return String.format("Villain: "+resultSet.getString(1));
    }

    private static String findVillainNameById(int villainId) throws SQLException {
        PreparedStatement preparedStatement=connection.prepareStatement(
                "select name from  villains " +
                        "where  id=?;");
        preparedStatement.setInt(1,villainId);
        ResultSet resultSet=preparedStatement.executeQuery();

           resultSet.next();
           return resultSet.getString("name");
    }

    private static Connection getConnection() throws IOException, SQLException {
//        System.out.println("Enter user:");
//        String user=reader.readLine();
//        System.out.println("Enter password");
//        String password=reader.readLine();

        Properties properties=new Properties();
        properties.setProperty("user","root");
        properties.setProperty("password","123456");

        return DriverManager.getConnection(CONNECTION_STRING + DB_NAME, properties);
    }
}