/**
 * Created by orange on 2015/3/25.
 */
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
import dnl.utils.text.table.TextTable;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


import java.util.*;

public class RPCClient {

    public static void main(String[] argv) {
        RPCConnection myClient=null;
        while (true) {
            try {
                // IDE 中输入与输出
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter Username: ");
                String name = sc.nextLine();
                System.out.print("Enter Password: ");
                String password = sc.nextLine();

                // 控制台中输入与输出
                /*
                java.io.Console cons = System.console();
                String name=null,password=null;
                if (cons != null)
                {              // 判断是否有控制台的使用权
                    name = new String(cons.readLine("Please input your name: "));      // 读取整行字符
                    password = new String(cons.readPassword("Please input your password: "));   // 读取密码,输入时不显示
                }
                */

                if(name.equals("")||password.equals(""))
                {
                    System.out.println("please enter valid username or password");
                    continue;
                }

                // 连接服务器验证用户名与密码
                String jsonString="{\"name\":\""+name+"\", \"password\":\""+password+"\", \"service\":\"check\"}";//密码校验

                if(myClient==null) {
                    try {
                        myClient = new RPCConnection();
                    } catch (Exception e) {
                        //连接服务器出错
                        System.out.println("Error: 连接远程服务器失败");
                        continue;
                    }
                }

                String myJsonResponse=null;
                String state = null;
                try{

                    myJsonResponse=myClient.call(jsonString);
                    //jsonResponse解析，获取状态信息
                    JSONObject jsonObject = JSONObject.fromObject(myJsonResponse);
                    state=jsonObject.getString("msg");
                }
                catch  (Exception e) {
                    e.printStackTrace();
                }

                if (state.equals("ok"))
                // 用户名，密码正确
                {
                    System.out.println("Login Success");

                    // 继续输入sql语句等
                    String lines;
                    System.out.print("SQL> ");
                    while (!(lines = sc.nextLine()).equals("quit"))//如果不输入quit则一直输入
                    {
                        lines="select * from user";
                        if(lines.isEmpty()) {
                            System.out.print("SQL> ");
                            continue;
                        }
                        if (lines.lastIndexOf(';') == lines.length() - 1) {
                            lines = lines.substring(0, lines.length()-1);
                        }

                        lines="{\"service\":\"sqlExecute\",\"sql\":\""+lines+"\"}";
                        String json = myClient.call(lines);

                        /* json 数据解析并打印 */
                        myClient.jsonparser(json);

                        System.out.print("SQL> ");
                    }
                    break;
                } else if (state.equals("no"))
                // 用户名，密码不正确
                {
                    System.out.println("Wrong Username, Password or Account Number. Please try again");
                    // 再次输入用户名密码
                }
                else {
                    // 出现问题（如传输过程等问题）
                    System.out.println(state);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        if (myClient!= null) {
            try {
                myClient.close();
            } catch (Exception ignore) {
            }
        }

    }
}
