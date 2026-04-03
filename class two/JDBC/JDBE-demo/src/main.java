import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class main {
    public static void main(String[] args)throws Exception {
        //1.注册驱动
        Class.forName("com.mysql.cj.jdbc.Driver");
        //2.获得链接
        String url="jdbc:mysql://127.0.0.1:3306/text";
        String user="root";
        String password="liyouxuan81";
        Connection conn=DriverManager.getConnection(url,user,password);
        String sql="UPDATE cs_students SET dorm_address = '杏园公寓2幢303室' WHERE student_id = '202411010101'";
        Statement stmt=conn.createStatement();
        int cnt =stmt.executeUpdate(sql);
        System.out.println(cnt>0?"修改成功":"修改失败");
        //7.释放资源
        stmt.close();
        conn.close();
    }
}
