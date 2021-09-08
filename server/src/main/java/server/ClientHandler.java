package server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler {
    DataInputStream in;
    DataOutputStream out;
    Server server;
    Socket socket;

    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Client connected " + socket.getRemoteSocketAddress());

            new Thread(() -> {
                try {
                    socket.setSoTimeout(50000);

                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/reg ")) {
                            String[] token = str.split("\\s");
                            if (token.length < 4) {
                                continue;
                            }
                            boolean b = server.getAuthService()
                                    .registration(token[1], token[2], token[3]);
                            if (b) {
                                sendMsg("/regok");
                            } else {
                                sendMsg("/regno");
                            }
                        }

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split("\\s");
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                login = token[1];
                                if (!server.isLoginAuthenticated(login)) {
                                    nickname = newNick;
                                    sendMsg("/authok " + newNick);
                                    getLastMessage();
                                    server.subscribe(this);
                                    socket.setSoTimeout(0);
                                    break;
                                } else {
                                    sendMsg("С этим логином уже вошли в чат");
                                }
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }
                            if (str.startsWith("/w ")) {
                                String[] token = str.split("\\s", 3);
                                if (token.length < 3) {
                                    continue;
                                }
                                server.privateMsg(this, token[1], token[2]);
                            }
                            String[] subStr = str.split(" ");
                            if (subStr[0].equals("/renickname")){
                                server.rename(nickname, subStr[1]);
                                nickname = subStr[1];
                            }
                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }

                } catch (SocketTimeoutException e) {
                    sendMsg("/end");
                    System.out.println("Client disconnected by timeout");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Client disconnected " + socket.getRemoteSocketAddress());
                    try {
                        socket.close();
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getLastMessage() throws IOException {
//        try {
//            File file = new File("LogServer.txt");
//            FileReader fr = new FileReader(file);
//            BufferedReader reader = new BufferedReader(fr);
//            String line = reader.readLine();
//            while (line != null) {
//                System.out.println(line);
//                sendMsgNoLog(line);
//                line = reader.readLine();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        List<String> tmp = new ArrayList<String>();
        String strpath="LogServer.txt";
        FileReader fr = null;
        try {
            fr = new FileReader(strpath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);
        String ch;

        do {
            ch = br.readLine();
            tmp.add(ch);
         } while (ch != null);

        int limiter = 0;
        for(int i=tmp.size()-2;i>=0;i--) {
            limiter++;
            if(limiter <= 20) {
                sendMsgNoLog(tmp.get(i));
                System.out.println(tmp.get(i));
            }
            else{
                break;
            }
        }

    }


    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
            writeLog(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsgNoLog(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeLog(String msg){
        if(!msg.startsWith("/")){
            try(FileWriter writer = new FileWriter("LogServer.txt", true))
            {
                writer.write(msg + "\n");
                writer.flush();
            }
            catch(IOException ex){
                System.out.println(ex.getMessage());
            }
          }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
