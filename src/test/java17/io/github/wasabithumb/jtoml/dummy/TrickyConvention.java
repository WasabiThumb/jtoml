package io.github.wasabithumb.jtoml.dummy;

import io.github.wasabithumb.jtoml.Faker;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.reflect.Convention;
import io.github.wasabithumb.jtoml.serial.reflect.Key;

public final class TrickyConvention implements TomlSerializable {

    public static TrickyConvention create() {
        return Faker.create(TrickyConvention.class);
    }

    //

    @Convention.Literal public int literalInt;
    @Convention.Lower public int lowerInt;
    public int defaultingInt;
    @Convention.Kebab public int kebabInt;
    @Convention.Split public int splitInt;
    @Key("custom") public int customInt;

    //

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TrickyConvention other)) return false;
        return this.literalInt == other.literalInt &&
                this.lowerInt == other.lowerInt &&
                this.defaultingInt == other.defaultingInt &&
                this.kebabInt == other.kebabInt &&
                this.splitInt == other.splitInt &&
                this.customInt == other.customInt;
    }

}
