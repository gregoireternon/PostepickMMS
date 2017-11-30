package postepick.mms;

import android.os.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Gregoire on 14/11/2017.
 */

public class MessageEntity {



    public static enum Type{
        SMS,
        MMS
    }

    Type _type;

    String _content;

    String _phoneNumber;

    Date _msgDate;

    List<String> _imageNames = new ArrayList<>();

    public Type getType() {
        return _type;
    }

    public void setType(Type _type) {
        this._type = _type;
    }

    public String getContent() {
        return _content;
    }

    public void setContent(String _content) {
        this._content = _content;
    }

    public String getPhoneNumber() {
        return _phoneNumber;
    }

    public void setPhoneNumber(String _phoneNumber) {
        this._phoneNumber = _phoneNumber;
    }

    public Date getMsgDate() {
        return _msgDate;
    }

    public void setMsgDate(Date _msgDate) {
        this._msgDate = _msgDate;
    }

    public List<String> getImageNames() {
        return this._imageNames;
    }
}
