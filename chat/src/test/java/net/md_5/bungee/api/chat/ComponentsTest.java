package net.md_5.bungee.api.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.Assert;
import org.junit.Test;

public class ComponentsTest
{

    @Test
    public void testBuilderClone()
    {
        ComponentBuilder builder = new ComponentBuilder( "Hel" ).color( ChatColor.RED ).append( "lo" ).color( ChatColor.DARK_RED );
        ComponentBuilder cloned = new ComponentBuilder( builder );

        Assert.assertEquals( TextComponent.toLegacyText( builder.create() ), TextComponent.toLegacyText( cloned.create() ) );
    }

    @Test
    public void testBuilderAppend()
    {
        ClickEvent clickEvent = new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/help " );
        HoverEvent hoverEvent = new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Hello world" ).create() );

        ComponentBuilder builder = new ComponentBuilder( "Hello " ).color( ChatColor.YELLOW );
        builder.append( new ComponentBuilder( "world!" ).color( ChatColor.GREEN ).event( hoverEvent ).event( clickEvent ).create() );

        BaseComponent[] components = builder.create();

        Assert.assertEquals( components[1].getHoverEvent(), hoverEvent );
        Assert.assertEquals( components[1].getClickEvent(), clickEvent );
        Assert.assertEquals( "Hello world!", BaseComponent.toPlainText( components ) );
        Assert.assertEquals( ChatColor.YELLOW + "Hello " + ChatColor.GREEN + "world!", BaseComponent.toLegacyText( components ) );
    }

    @Test
    public void testBasicComponent()
    {
        TextComponent textComponent = new TextComponent( "Hello world" );
        textComponent.setColor( ChatColor.RED );

        Assert.assertEquals( "Hello world", textComponent.toPlainText() );
        Assert.assertEquals( ChatColor.RED + "Hello world", textComponent.toLegacyText() );
    }

    @Test
    public void testLegacyConverter()
    {
        BaseComponent[] test1 = TextComponent.fromLegacyText( ChatColor.AQUA + "Aqua " + ChatColor.RED + ChatColor.BOLD + "RedBold" );

        Assert.assertEquals( "Aqua RedBold", BaseComponent.toPlainText( test1 ) );
        Assert.assertEquals( ChatColor.AQUA + "Aqua " + ChatColor.RED + ChatColor.BOLD + "RedBold", BaseComponent.toLegacyText( test1 ) );

        BaseComponent[] test2 = TextComponent.fromLegacyText( "Text http://spigotmc.org " + ChatColor.GREEN + "google.com/test" );

        Assert.assertEquals( "Text http://spigotmc.org google.com/test", BaseComponent.toPlainText( test2 ) );
        //The extra ChatColor.WHITEs are sometimes inserted when not needed but it doesn't change the result
        Assert.assertEquals( ChatColor.WHITE + "Text " + ChatColor.WHITE + "http://spigotmc.org" + ChatColor.WHITE
                + " " + ChatColor.GREEN + "google.com/test", BaseComponent.toLegacyText( test2 ) );

        ClickEvent url1 = test2[1].getClickEvent();
        Assert.assertNotNull( url1 );
        Assert.assertTrue( url1.getAction() == ClickEvent.Action.OPEN_URL );
        Assert.assertEquals( "http://spigotmc.org", url1.getValue() );

        ClickEvent url2 = test2[3].getClickEvent();
        Assert.assertNotNull( url2 );
        Assert.assertTrue( url2.getAction() == ClickEvent.Action.OPEN_URL );
        Assert.assertEquals( "http://google.com/test", url2.getValue() );
    }

    @Test
    public void testTranslateComponent()
    {
        TranslatableComponent item = new TranslatableComponent( "item.swordGold.name" );
        item.setColor( ChatColor.AQUA );
        TranslatableComponent translatableComponent = new TranslatableComponent( "commands.give.success",
                item, "5",
                "thinkofdeath" );

        Assert.assertEquals( "Given Golden Sword * 5 to thinkofdeath", translatableComponent.toPlainText() );
        Assert.assertEquals( ChatColor.WHITE + "Given " + ChatColor.AQUA + "Golden Sword" + ChatColor.WHITE
                + " * " + ChatColor.WHITE + "5" + ChatColor.WHITE + " to " + ChatColor.WHITE + "thinkofdeath",
                translatableComponent.toLegacyText() );

        TranslatableComponent positional = new TranslatableComponent( "book.pageIndicator", "5", "50" );

        Assert.assertEquals( "Page 5 of 50", positional.toPlainText() );
        Assert.assertEquals( ChatColor.WHITE + "Page " + ChatColor.WHITE + "5" + ChatColor.WHITE + " of " + ChatColor.WHITE + "50", positional.toLegacyText() );
    }

    @Test
    public void testBuilder()
    {
        BaseComponent[] components = new ComponentBuilder( "Hello " ).color( ChatColor.RED ).
                append( "World" ).bold( true ).color( ChatColor.BLUE ).
                append( "!" ).color( ChatColor.YELLOW ).create();

        Assert.assertEquals( "Hello World!", BaseComponent.toPlainText( components ) );
        Assert.assertEquals( ChatColor.RED + "Hello " + ChatColor.BLUE + ChatColor.BOLD
                + "World" + ChatColor.YELLOW + ChatColor.BOLD + "!", BaseComponent.toLegacyText( components ) );
    }

    @Test
    public void testBuilderReset()
    {
        BaseComponent[] components = new ComponentBuilder( "Hello " ).color( ChatColor.RED )
                .append( "World" ).reset().create();

        Assert.assertEquals( components[0].getColor(), ChatColor.RED );
        Assert.assertEquals( components[1].getColor(), ChatColor.WHITE );
    }

    @Test
    public void testBuilderFormatRetention()
    {
        BaseComponent[] noneRetention = new ComponentBuilder( "Hello " ).color( ChatColor.RED )
                .append( "World", ComponentBuilder.FormatRetention.NONE ).create();

        Assert.assertEquals( noneRetention[0].getColor(), ChatColor.RED );
        Assert.assertEquals( noneRetention[1].getColor(), ChatColor.WHITE );

        HoverEvent testEvent = new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "test" ).create() );

        BaseComponent[] formattingRetention = new ComponentBuilder( "Hello " ).color( ChatColor.RED )
                .event( testEvent ).append( "World", ComponentBuilder.FormatRetention.FORMATTING ).create();

        Assert.assertEquals( formattingRetention[0].getColor(), ChatColor.RED );
        Assert.assertEquals( formattingRetention[0].getHoverEvent(), testEvent );
        Assert.assertEquals( formattingRetention[1].getColor(), ChatColor.RED );
        Assert.assertNull( formattingRetention[1].getHoverEvent() );

        ClickEvent testClickEvent = new ClickEvent( ClickEvent.Action.OPEN_URL, "http://www.example.com" );

        BaseComponent[] eventRetention = new ComponentBuilder( "Hello " ).color( ChatColor.RED )
                .event( testEvent ).event( testClickEvent ).append( "World", ComponentBuilder.FormatRetention.EVENTS ).create();

        Assert.assertEquals( eventRetention[0].getColor(), ChatColor.RED );
        Assert.assertEquals( eventRetention[0].getHoverEvent(), testEvent );
        Assert.assertEquals( eventRetention[0].getClickEvent(), testClickEvent );
        Assert.assertEquals( eventRetention[1].getColor(), ChatColor.WHITE );
        Assert.assertEquals( eventRetention[1].getHoverEvent(), testEvent );
        Assert.assertEquals( eventRetention[1].getClickEvent(), testClickEvent );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoopSimple()
    {
        TextComponent component = new TextComponent( "Testing" );
        component.addExtra( component );
        ComponentSerializer.toString( component );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoopComplex()
    {
        TextComponent a = new TextComponent( "A" );
        TextComponent b = new TextComponent( "B" );
        b.setColor( ChatColor.AQUA );
        TextComponent c = new TextComponent( "C" );
        c.setColor( ChatColor.RED );
        a.addExtra( b );
        b.addExtra( c );
        c.addExtra( a );
        ComponentSerializer.toString( a );
    }

    @Test
    public void testRepeated()
    {
        TextComponent a = new TextComponent( "A" );
        TextComponent b = new TextComponent( "B" );
        b.setColor( ChatColor.AQUA );
        a.addExtra( b );
        a.addExtra( b );
        ComponentSerializer.toString( a );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRepeatedError()
    {
        TextComponent a = new TextComponent( "A" );
        TextComponent b = new TextComponent( "B" );
        b.setColor( ChatColor.AQUA );
        TextComponent c = new TextComponent( "C" );
        c.setColor( ChatColor.RED );
        a.addExtra( b );
        a.addExtra( c );
        c.addExtra( a );
        a.addExtra( b );
        ComponentSerializer.toString( a );
    }

    @Test
    public void testInvalidColorCodes()
    {
        StringBuilder allInvalidColorCodes = new StringBuilder();

        // collect all invalid color codes (e.g. ??z, ??g, ...)
        for ( char alphChar : "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray() )
        {
            if ( ChatColor.ALL_CODES.indexOf( alphChar ) == -1 )
            {
                allInvalidColorCodes.append( ChatColor.COLOR_CHAR );
                allInvalidColorCodes.append( alphChar );
            }
        }

        // last char is a single '??'
        allInvalidColorCodes.append( ChatColor.COLOR_CHAR );

        String invalidColorCodesLegacyText = fromAndToLegacyText( allInvalidColorCodes.toString() );
        String emptyLegacyText = fromAndToLegacyText( "" );

        // all invalid color codes and the trailing '??' should be ignored
        Assert.assertEquals( emptyLegacyText, invalidColorCodesLegacyText );
    }

    private String fromAndToLegacyText(String legacyText)
    {
        return BaseComponent.toLegacyText( TextComponent.fromLegacyText( legacyText ) );
    }
}
