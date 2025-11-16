package net.minestom.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.level.block.state.properties.*;
import net.minestom.datagen.DataGenerator;

import java.lang.reflect.Field;
import java.util.List;

public final class BlockPropertyGenerator extends DataGenerator {
    @Override
    public JsonObject generate() {
        JsonObject properties = new JsonObject();

        for (Field field : BlockStateProperties.class.getFields()) {
            if(!Property.class.isAssignableFrom(field.getType()))
                continue;

            try {
                Property<?> property = (Property<?>) field.get(null);
                String name = field.getName();

                JsonObject propertyJson = new JsonObject();
                propertyJson.addProperty("name", property.getName());
                switch (property) {
                    case IntegerProperty intProperty -> {
                        List<Integer> values = intProperty.getPossibleValues();
                        int min = values.getFirst();
                        int max = values.getLast();

                        propertyJson.addProperty("type", "integer");
                        propertyJson.addProperty("min", min);
                        propertyJson.addProperty("max", max);
                    }
                    case EnumProperty<?> enumProperty -> {
                        propertyJson.addProperty("type", "enum");
                        propertyJson.addProperty("mojangName", enumProperty.getPossibleValues().getFirst().getClass().getSimpleName());
                        propertyJson.add("values", fillValues(enumProperty));
                    }
                    case BooleanProperty booleanProperty -> propertyJson.addProperty("type", "boolean");
                    default ->
                            throw new IllegalStateException("Unknown property type: " + property.getClass().getName());
                }

                properties.add(name, propertyJson);

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return properties;
    }

    private <T extends Comparable<T>> JsonArray fillValues(Property<T> property) {
        JsonArray values = new JsonArray();
        for (T value : property.getPossibleValues()) {
            if(!(value instanceof Enum<?> enumValue)) {
                values.add(new JsonPrimitive(property.getName(value)));
                continue;
            }

            JsonObject json = new JsonObject();
            json.addProperty("enumName", enumValue.name());
            json.addProperty("serializedName", property.getName(value));
            values.add(json);
        }
        return values;
    }

}
