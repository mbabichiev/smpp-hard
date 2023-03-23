package com.gmail.foy.maxach.repositories;

import com.gmail.foy.maxach.database.MessageDB;
import com.gmail.foy.maxach.models.Message;

public class MessageRepository {

    private static final MessageDB messageDB = new MessageDB();

    public void create(Message message) {

        String SQL_INSERT_INTO_TABLE = "INSERT INTO messages" +
                "(" +
                    "id," +
                    "publish_date," +
                    "from_addr," +
                    "from_ton," +
                    "from_npi," +
                    "to_addr," +
                    "to_ton," +
                    "to_npi," +
                    "dcs," +
                    "delivery_status," +
                    "message" +
                ") VALUES (" +
                "'" + message.getId() + "'," +
                "" + message.getPublishDate() + "," +
                "'" + message.getFromAddr() + "'," +
                "'" + message.getFromTON() + "'," +
                "'" + message.getFromNPI() + "'," +
                "'" + message.getToAddr() + "'," +
                "'" + message.getToTON() + "'," +
                "'" + message.getToNPI() + "'," +
                "'" + message.getDcs() + "'," +
                "" + message.getDeliveryStatus() + "," +
                "'" + message.getMessage() + "')";

        messageDB.createStatement(SQL_INSERT_INTO_TABLE);
    }


    public void updateStatus(String messageId) {
        String SQL_UPDATE_BY_MESSAGE_ID = "UPDATE messages SET " +
                "delivery_status = 1 WHERE id = '" + messageId + "';";

        messageDB.createStatement(SQL_UPDATE_BY_MESSAGE_ID);
    }

}
