package postepick.mms;

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
}
