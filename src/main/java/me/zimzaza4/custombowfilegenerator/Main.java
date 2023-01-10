package me.zimzaza4.custombowfilegenerator;

import com.google.gson.*;
import com.loohp.yamlconfiguration.YamlConfiguration;
import com.loohp.yamlconfiguration.libs.javax.json.Json;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {


    public static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    public static void main(String[] args) throws IOException {

        String namespace = "bow";

        if (args.length > 0) {
            namespace = args[0];
        }

        String prefix = namespace + ":";

        File targetFolder = new File("target");

        File bowsFolder = new File("bows");

        File originalAttachableJsonFile = new File("template.json");

        File originalItemTextureJsonFile = new File("original_item_texture.json");

        File originalPlayerAnimationControllerJsonFile = new File("player.animation_controllers.json");

        if (!originalPlayerAnimationControllerJsonFile.exists() || !originalItemTextureJsonFile.exists() || !bowsFolder.exists() || !originalAttachableJsonFile.exists()) {
            System.out.println("缺少文件");
            return;
        }

        JsonObject animationController = JsonParser.parseReader(new FileReader(originalPlayerAnimationControllerJsonFile)).getAsJsonObject();

        JsonObject itemTexture = JsonParser.parseReader(new FileReader(originalItemTextureJsonFile)).getAsJsonObject();

        JsonObject templateAttachable = JsonParser.parseReader(new FileReader(originalAttachableJsonFile)).getAsJsonObject();

        List<CustomBow> bowList = new ArrayList<>();

        for (File bowFolder : bowsFolder.listFiles()) {
            String id = bowFolder.getName();
            CustomBow bow = new CustomBow();
            bow.id = id;
            bow.textureFile = bowFolder.listFiles();
            bow.texturePathList = new ArrayList<>();
            bowList.add(bow);
        }


        JsonArray animations = animationController
                .get("animation_controllers").getAsJsonObject()
                .get("controller.animation.player.root").getAsJsonObject()
                .get("states").getAsJsonObject()
                .get("third_person").getAsJsonObject()
                .get("animations").getAsJsonArray();

        File attachablesFile = new File(targetFolder, "attachables");
        File texturesFile = new File(targetFolder, "textures/items/bows");
        attachablesFile.mkdirs();
        texturesFile.mkdirs();

        JsonObject useItemProgress = new JsonObject();
        String conditionsValue = "";
        String condition = "";
        List<String> names = new ArrayList<>();

        JsonObject bowEquipped = new JsonObject();
        String conditionsValue2 = "";
        String condition2 = "";
        List<String> names2 = new ArrayList<>();
        for (JsonElement element : animations) {
            if (element.isJsonObject()) {
                if (element.getAsJsonObject().has("use_item_progress")) {
                    conditionsValue = element.getAsJsonObject()
                            .get("use_item_progress")
                            .getAsString();

                    condition = StringUtils.substringBetween(conditionsValue, "query.is_item_name_any(", ")");
                    String[] array = condition.split(",");
                    names.addAll(List.of(array));

                    useItemProgress = element.getAsJsonObject();
                } else if (element.getAsJsonObject().has("third_person_bow_equipped")) {
                    conditionsValue2 = element.getAsJsonObject()
                            .get("third_person_bow_equipped")
                            .getAsString();


                    condition2 = StringUtils.substringBetween(conditionsValue2, "query.is_item_name_any(", ")");

                    String[] array = condition2.split(",");
                    names2.addAll(List.of(array));

                    bowEquipped = element.getAsJsonObject();
                }
            }
        }


        for (CustomBow bow : bowList) {
            File texture = new File(texturesFile, bow.id);
            File attachable = new File(attachablesFile, bow.id + ".attachable.json");

            names.add(names.size() - 1, "'" + prefix + bow.id + "'");
            names2.add(names.size() - 1, "'" + prefix + bow.id + "'");

            for (File imageFile : bow.textureFile) {
                texture.mkdirs();
                Files.copy(imageFile.toPath(), new File(texture, imageFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                String path = "textures/items/bows/" + bow.id + "/" + imageFile.getName().replace(".png", "");
                bow.texturePathList.add(path);
                if (imageFile.getName().contains("standby")) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("textures", path);
                    itemTexture.get("texture_data").getAsJsonObject().add(bow.id, obj);
                }
            }
            JsonObject attachableJson = templateAttachable.deepCopy();
            JsonObject content = attachableJson.get("minecraft:attachable")
                    .getAsJsonObject();

            content.get("description")
                    .getAsJsonObject()
                    .addProperty("identifier", prefix + bow.id);

            JsonObject textures = new JsonObject();
            for (String path : bow.texturePathList) {
                if (path.contains("standby")) {
                    textures.addProperty("default", path);
                }
                if (path.contains("pulling_0")) {
                    textures.addProperty("bow_pulling_0", path);
                }
                if (path.contains("pulling_1")) {
                    textures.addProperty("bow_pulling_1", path);
                }
                if (path.contains("pulling_2")) {
                    textures.addProperty("bow_pulling_2", path);
                }
                if (path.contains("pulling_3")) {
                    textures.addProperty("bow_pulling_3", path);
                }

            }
            textures.addProperty("enchanted", "textures/misc/enchanted_item_glint");
            content.get("description")
                    .getAsJsonObject()
                    .add("textures", textures);
            attachable.createNewFile();

            FileWriter writer = new FileWriter(attachable);
            writer.write(GSON.toJson(attachableJson));
            writer.close();

        }

        useItemProgress.addProperty("use_item_progress", conditionsValue.replace(condition, String.join(",", names)));
        bowEquipped.addProperty("third_person_bow_equipped", conditionsValue2.replace(condition2, String.join(",", names2)));


        FileWriter writer = new FileWriter(new File(targetFolder, "textures/item_texture.json"));
        writer.write(GSON.toJson(itemTexture));
        writer.close();

        new File(targetFolder, "animation_controllers").mkdirs();
        writer = new FileWriter(new File(targetFolder, "animation_controllers/player.animation_controllers.json"));
        writer.write(GSON.toJson(animationController));
        writer.close();


    }
}
