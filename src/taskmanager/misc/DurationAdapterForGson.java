package taskmanager.misc;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapterForGson extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter out, Duration value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toMillis());
        }
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        if (in.peek() == null) {
            return null;
        }
        long millis = in.nextLong();
        return Duration.ofMillis(millis);
    }
}
