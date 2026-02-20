package a12bc34.commandperplayer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MojMenuScreen extends Screen {
    private TextFieldWidget prefixInput;
    private TextFieldWidget suffixInput;
    private TextFieldWidget searchInput;
    private String prefixStr = "ban";
    private String suffixStr = "cheating";
    private String searchStr = "";
    private Float tbcValue = (float)0.2;

    private List<String> allPlayers = new ArrayList<>();
    private List<String> selectedPlayers = new ArrayList<>();
    private List<String> filteredPlayers = new ArrayList<>();
    int listposX = 5; //20
    int listposY = 50; // 50 yeah were going back to 50 cause 20 is offscreen
    int listWidth = 100;
    int listHeight = 240;
    int tick = 0;
    int calls = 0;
    String localName;

    {
        assert this.client.player != null;
        localName = this.client.player.getDisplayName().getString();
    }

    private double scroll = 0;
    private double timeBetweenCommand = 1.0;
    public MojMenuScreen(Text title) {
        super(title);
    }
    private boolean isInBox(int pointX, int pointY, int x, int y, int w, int h){
        if (pointX >= x && pointX <= (x+w)){
            return pointY >= y && pointY <= (y + h);
        }
        return false;
    };
    @Override
    protected void init() {
        int xCenter = this.width / 2;
        int yCenter = this.height / 2;
        updateList();

        this.searchInput = new TextFieldWidget(this.textRenderer, listposX, listposY-25, listWidth, 20, Text.literal("Search"));
        this.searchInput.setText(searchStr);
        this.searchInput.setPlaceholder(Text.literal("Search"));
        this.searchInput.setChangedListener(this::onSearchChanged);
        this.addSelectableChild(this.searchInput);

        this.prefixInput = new TextFieldWidget(this.textRenderer, listposX+listWidth+ 8, listposY+16, 175, 20, Text.literal("Prefix"));
        this.prefixInput.setText(prefixStr);
        this.prefixInput.setPlaceholder(Text.literal("prefix (command)"));
        this.prefixInput.setChangedListener(this::onPrefixChanged);
        this.addSelectableChild(this.prefixInput);

        this.suffixInput = new TextFieldWidget(this.textRenderer, listposX+listWidth+ 8, listposY+48, 175, 20, Text.literal("Suffix"));
        this.suffixInput.setText(suffixStr);
        this.suffixInput.setPlaceholder(Text.literal("suffix"));
        this.suffixInput.setChangedListener(this::onSuffixChanged);
        this.addSelectableChild(this.suffixInput);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Select all"), button -> {
            for (String name : filteredPlayers) {
                if (!name.equals("null") && !selectedPlayers.contains(name)) {
                    selectedPlayers.add(name);
                }
            }
        }).dimensions(listposX+105, listposY-25, 75, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            selectedPlayers.clear();
            updateList();
        }).dimensions(listposX+185, listposY-25, 75, 20).build());

        this.addDrawableChild(new SliderWidget(listposX+listWidth+ 8, listposY+80, 175, 20, Text.literal("Time between commands: 1.0"), tbcValue) {
            @Override
            protected void updateMessage() {
                double numvalue = 0.1 + this.value * 4.9;
                tbcValue = (float) this.value;
                this.setMessage(Text.literal("Time between commands: " + String.format("%.1f",numvalue)));
            }
            @Override
            protected void applyValue() {
                MojMenuScreen.this.timeBetweenCommand = 0.1 + tbcValue * 4.9;
            }
        });

        this.addDrawableChild(ButtonWidget.builder(Text.literal("run"), button -> {
            if (this.client != null && this.client.player != null && !selectedPlayers.isEmpty()) {
                if(calls==0){calls = selectedPlayers.size();};
            }
        }).dimensions(listposX+listWidth+ 8, listposY+112, 175, 20).build());
    }
    private void onSearchChanged(String search) {
        this.searchStr=(search);
        updateList();
        filteredPlayers = allPlayers.stream()
                .filter(name -> name.toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
    }
    private void onPrefixChanged(String str) {this.prefixStr=str;}
    private void onSuffixChanged(String str){this.suffixStr=str;}


    @Override
    public void tick() {
        super.tick();
        if(calls > 0){
            tick++;
            if(tick >= (timeBetweenCommand*20)) {
                assert client.player != null; //  ts works intstead of if
                String cutprefix = prefixInput.getText();
                if(!cutprefix.isBlank()){
                    if (cutprefix.charAt(0) == '/'){
                        cutprefix = cutprefix.substring(1);
                    }
                }

                client.player.networkHandler.sendChatCommand(cutprefix + " " +  selectedPlayers.get(0) + " " +  suffixInput.getText());
                selectedPlayers.remove(0);
                calls--;
                tick = 0;
            }
        }
    }
    private void updateList() {
        scroll = 0;
        if (this.client != null && this.client.getNetworkHandler() != null) {
            Collection<PlayerListEntry> entries = this.client.getNetworkHandler().getPlayerList();
            allPlayers = entries.stream()
                    .map(entry -> {
                        if (entry.getProfile().name() != null) {
                            return entry.getProfile().name();
                        }
                        return "null";
                    })
                    .collect(Collectors.toList());
            if (this.searchInput != null) {
                filteredPlayers = new ArrayList<>();
                filteredPlayers = allPlayers.stream()
                        .filter(name -> name.toLowerCase().contains(this.searchInput.toString().toLowerCase()))
                        .collect(Collectors.toList());
            }else{
                filteredPlayers = new ArrayList<>(allPlayers);};
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scroll -= verticalAmount;
        if (scroll <= 0) {
            scroll = 0;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        if (click.button() == 0) {
            for (int i = 0; i < Math.min(filteredPlayers.size(), 15); i++) {
                int scrollnums = (int) Math.floor(this.scroll /4);
                if(!((i+scrollnums >= filteredPlayers.size()) || (i+scrollnums < 0))){
                    String name = filteredPlayers.get(i+scrollnums);
                    if(isInBox((int) mouseX, (int) mouseY,listposX+2,listposY + 2 + (i * 12),listWidth,12)){
                        if(selectedPlayers.contains(name)){
                            selectedPlayers.remove(name);
                        }else{
                            selectedPlayers.add(name);};
                    };
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawTextWithShadow(this.textRenderer, "Commands per player - a12bc34 | Only on Modirinth", listposX, 8, 0xFFFFFFFF);
        this.searchInput.render(context, mouseX, mouseY, delta);
        this.prefixInput.render(context, mouseX, mouseY, delta);
        this.suffixInput.render(context, mouseX, mouseY, delta);
        context.fill(listposX,listposY,listWidth,listHeight, 0x55000000);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, 50, 10, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Selected: " + selectedPlayers.size(), listposX+listWidth+8, listposY, 0xFFFFFFFF);
        if(!prefixInput.getText().isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, "Preview:", listposX + listWidth + 8, listposY + 134, 0xFFFFFFFF);
            String cutprefixButHere = prefixInput.getText();
            if (!cutprefixButHere.isBlank()) {
                if (cutprefixButHere.charAt(0) == '/') {
                    cutprefixButHere = cutprefixButHere.substring(1);
                }
            }
            context.drawTextWithShadow(this.textRenderer, localName + "> /" + cutprefixButHere + " Player " + this.suffixInput.getText(), listposX + listWidth + 8, listposY + 146, 0xFFFFFFFF);
        }
        //tuff text :fire:
        for (int i = 0; i < Math.min(filteredPlayers.size(), 15); i++) {
            int scrollnums = (int) Math.floor(this.scroll /4);
            if(!((i+scrollnums >= filteredPlayers.size()) || (i+scrollnums < 0))){
                String name = filteredPlayers.get(i+scrollnums);
                if(selectedPlayers.contains(name)){
                    context.drawTextWithShadow(this.textRenderer, name, listposX + 2, listposY + 2 + (i * 12), 0xFF00FF00);
                }else {
                    context.drawTextWithShadow(this.textRenderer, name, listposX + 2, listposY + 2 + (i * 12), 0xFFFFFFFF);
                }
            }
        }

    }
    @Override
    public boolean shouldPause() {
        return false;
    }
}