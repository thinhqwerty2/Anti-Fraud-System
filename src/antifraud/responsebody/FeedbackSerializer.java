package antifraud.responsebody;

import antifraud.entity.Feedback;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import java.io.IOException;

@Component
public class FeedbackSerializer extends StdSerializer<Feedback> {

    private static final long serialVersionUID = 1L;

    public FeedbackSerializer() {
        this(null);
    }

    public FeedbackSerializer(Class<Feedback> t) {
        super(t);
    }

    @Override
    public void serialize(Feedback value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeString("");
        } else {
            gen.writeString(value.toString());
        }
    }

}