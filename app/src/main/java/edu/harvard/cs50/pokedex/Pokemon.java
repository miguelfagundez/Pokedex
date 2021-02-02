package edu.harvard.cs50.pokedex;

public class Pokemon {
    private String name;
    private String url;
    public boolean catchPokemon;

    Pokemon(String name, String url) {
        this.name = name;
        this.url = url;
        this.catchPokemon = false;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setCatchPokemon(boolean value){
        this.catchPokemon = value;
    }

    public boolean getCatchPokemon(){
        return this.catchPokemon;
    }
}
