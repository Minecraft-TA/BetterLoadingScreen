package alexiil.mods.load;

import java.awt.SplashScreen;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lwjgl.opengl.GL11;

import alexiil.mods.load.ProgressDisplayer.IDisplayer;
import alexiil.mods.load.json.Area;
import alexiil.mods.load.json.EPosition;
import alexiil.mods.load.json.EType;
import alexiil.mods.load.json.ImageRender;
import alexiil.mods.load.json.JsonConfig;
import cpw.mods.fml.client.FMLFileResourcePack;
import cpw.mods.fml.client.FMLFolderResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

public class MinecraftDisplayer implements IDisplayer {
    private static String sound;
    private static String defaultSound = "random.levelup";
    private static String fontTexture;
    private static String defaultFontTexture = "textures/font/ascii.png";
    private final boolean preview;
    private ImageRender[] images;
    private TextureManager textureManager = null;
    private Map<String, FontRenderer> fontRenderers = new HashMap<String, FontRenderer>();
    private FontRenderer fontRenderer = null;
    private ScaledResolution resolution = null;
    private Minecraft mc = null;
    private boolean callAgain = false;
    private IResourcePack myPack;
    private float clearRed = 1, clearGreen = 1, clearBlue = 1;
    private boolean hasSaidNice = false;
    private float lastPercent = 0;
    private String GTprogress = "betterloadingscreen:textures/GTMaterialsprogressBars.png";
    private String progress = "betterloadingscreen:textures/mainProgressBar.png";
    private String GTprogressAnimated = "betterloadingscreen:textures/GTMaterialsprogressBars.png";
    private String progressAnimated = "betterloadingscreen:textures/mainProgressBar.png";
    private String title = "betterloadingscreen:textures/transparent.png";
    private String background = "betterloadingscreen:textures/backgrounds/background1.png";
    private int[] titlePos = new int[] {0, 0, 256, 256, 0, 50, 187, 145};
    private int[] GTprogressPos = new int[] {0, 0, 172, 12, 0, -83, 172, 6};
    private int[] GTprogressPosAnimated = new int[] {0, 12, 172, 12, 0, -83, 172, 6};
    private int[] progressPos = new int[] {0, 0, 194, 24, 0, -50, 194, 16};
    private int[] progressPosAnimated = new int[] {0, 24, 194, 24, 0, -50, 194, 16};
    private int[] progressTextPos = new int[] {0, -30};
    private int[] progressPercentagePos = new int[] {0, -40};
    private int[] GTprogressTextPos = new int[] {0, -65};
    private int[] gtptp = GTprogressTextPos;
    private int[] GTprogressPercentagePos = new int[] {0, -75};
    private boolean textShadow = true;
    private String textColor = "ffffff";
    private boolean randomBackgrounds  = false;
    private String[] randomBackgroundArray = new String[] {"betterloadingscreen:textures/backgrounds/background1.png", "betterloadingscreen:textures/backgrounds/background2.png"};
    public static boolean isNice = false;
    public static boolean isRegisteringGTmaterials;
    public static boolean isReplacingVanillaMaterials = false;
    public static boolean isRegisteringBartWorks = false;
    private Logger log;
    
    public static void playFinishedSound() {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        ResourceLocation location = new ResourceLocation(sound);
        SoundEventAccessorComposite snd = soundHandler.getSound(location);
        if (snd == null) {
            System.out.println("The sound given (" + sound + ") did not give a valid sound!");
            location = new ResourceLocation(defaultSound);
            snd = soundHandler.getSound(location);
        }
        if (snd == null) {
            System.out.println("Default sound did not give a valid sound!");
            return;
        }
        ISound sound = PositionedSoundRecord.func_147673_a(location);
        soundHandler.playSound(sound);
    }

    public MinecraftDisplayer() {
        this(false);
    }

    public MinecraftDisplayer(boolean preview) {
        this.preview = preview;
    }

    @SuppressWarnings("unchecked")
    private List<IResourcePack> getOnlyList() {
        Field[] flds = mc.getClass().getDeclaredFields();
        for (Field f : flds) {
            if (f.getType().equals(List.class) && !Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                try {
                    return (List<IResourcePack>) f.get(mc);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void openPreview(ImageRender[] renders) {
        mc = Minecraft.getMinecraft();
        images = renders;
    }
    
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    
    public int[] stringToIntArray(String str) {
    	str = str.replaceAll("\\s+","");
    	String intBuffer = "";
    	List<Integer> numbers = new ArrayList<Integer>();
    	for (int i = 0; i < str.length(); i++) {
    		if (isNumeric(String.valueOf(str.charAt(i))) || String.valueOf(str.charAt(i)).equals("-")) {
				intBuffer += String.valueOf(str.charAt(i));
			}
    		if (String.valueOf(str.charAt(i)).equals(",") || String.valueOf(str.charAt(i)).equals("]")) {
    			numbers.add(Integer.parseInt(intBuffer));
    			intBuffer = "";
    		}
    	}
    	int[] res = new int[numbers.size()];
    	for (int i = 0; i < numbers.size(); i++) {
    		res[i] = numbers.get(i);
    	}
    	return res;
    }
    
    public String intArrayToString(int[] array) {
    	String res = "[";
    	for (int i = 0; i < array.length; i++) {
    		res += String.valueOf(array[i]);
    		if (i != array.length-1) {
    			 res += ", ";
			} else {
				res += "]";
			}
    	}
    	return res;
    }
    
    //after some thinking, this function is quite muda
    /*public boolean isSystemLineSeparatorAtIndex(String str, int index) {
    	boolean res = false;
    	
    	return res;
    }*/
    
    public String parseBackgroundArraytoCFGList(String[] backgrounds) {
    	String res = "{";//+System.lineSeparator();
    	for (int i = 0; i < backgrounds.length; i++) {
			res += "" + backgrounds[i];
			if (i < backgrounds.length - 1) {
				res += ", ";//+System.lineSeparator();
			}
		}
    	res += "}";
    	return res;
    }

    public String[] parseBackgroundCFGListToArray(String backgrounds) {
    	String[] res;
    	int numberOfBackgrounds = 1;
    	String stringBuffer = "";
    	for (int i = 0; i < backgrounds.length(); i++) {
    		if (String.valueOf(backgrounds.charAt(i)).equals(",")) {
    			numberOfBackgrounds++;
    		}
    	}
    	res = new String[numberOfBackgrounds];
    	for (int i = 0, j = 0; i < backgrounds.length(); i++) {
    		if (!String.valueOf(backgrounds.charAt(i)).equals("{") && !String.valueOf(backgrounds.charAt(i)).equals("}") && !String.valueOf(backgrounds.charAt(i)).equals(",") && !String.valueOf(backgrounds.charAt(i)).equals(" ") && !String.valueOf(backgrounds.charAt(i)).equals("\t")) {
    			stringBuffer += String.valueOf(backgrounds.charAt(i));
    		}
    		if (String.valueOf(backgrounds.charAt(i)).equals(",") || String.valueOf(backgrounds.charAt(i)).equals("}")) {
    			res[j] = stringBuffer;
    			stringBuffer = "";
    			j++;
    		}
    	}
    	return res;
    }
    
    // Minecraft's display hasn't been created yet, so don't bother trying
    // to do anything now
    @Override
    public void open(Configuration cfg) {
        mc = Minecraft.getMinecraft();
        String n = System.lineSeparator();
        // Open the normal config
        /*String commentBruh = "bruh"+ "\n";
        String bruh = cfg.getString("bruhissimo", "general", "false", commentBruh);
        System.out.println("Brih is: "+bruh);*/
        
        String comment4 = "What sound to play when loading is complete. Default is the level up sound (" + defaultSound + ")";
        sound = cfg.getString("sound", "general", defaultSound, comment4);

        comment4 = "What font texture to use? Special Cases:"
                + n +" - If you use the Russian mod \"Client Fixer\" then change this to \"textures/font/ascii_fat.png\"" + n;
        fontTexture = cfg.getString("font", "general", defaultFontTexture, comment4);
        
        String comment5 = "Path to background resource."+ n +"You can use a resourcepack"
        		+ " or resource loader for custom resources.";
        background = cfg.getString("background", "layout", background, comment5);
        String comment6 = "Path to logo/title resource";
        title = cfg.getString("title", "layout", title, comment6);
        String comment7 = "Logo coordinates in image and position."+ n +"the first four values indicate where the logo is located"
        		+ " on the image (you could use a spritesheet), the four next ones tell where the image will be located on screen"
        		+ n + "like this: [xLocation, yLocation, xWidth, yWidth, xLocation, yLocation, xWidth, yWidth]" + n + 
        		"The same is used for other images, except the background, which is fullscreen. Please ALWAYS provide an image, a transparent one if you want even."+n+
        		"CLS provides 'transparent.png'" + n +
        		"If you really insist ping me and I'll look if I can add an image or so. jackowski626#0522";
        titlePos = stringToIntArray(cfg.getString("titlePos", "layout", intArrayToString(titlePos), comment7));
        
        //Main Loading Bar Static
        String comment8 = "Path to main loading bar resource";
        progress = cfg.getString("mainProgressBar", "layout", progress, comment8);
        String comment9 = "Main loading bar position";
        progressPos = stringToIntArray(cfg.getString("mainProgressBarPos", "layout", intArrayToString(progressPos), comment9));
        //Main Loading Bar Animated
        String comment10 = "Path to animated main loading bar resource";
        progressAnimated = cfg.getString("mainProgressBarAnimated", "layout", progressAnimated, comment10);
        String comment11 = "Main animated loading bar position";
        progressPosAnimated = stringToIntArray(cfg.getString("mainProgressBarPosAnimated", "layout", intArrayToString(progressPosAnimated), comment11));
        //Main Loading Bar Text
        String comment12 = "Main loading bar text position. The four values are for positon.";
        progressTextPos = stringToIntArray(cfg.getString("mainProgressBarTextPos", "layout", intArrayToString(progressTextPos), comment12));
        //Main Loading Bar Percentage
        String comment13 = "Main loading bar percentage position";
        progressPercentagePos = stringToIntArray(cfg.getString("mainProgressBarPercentagePos", "layout", intArrayToString(progressPercentagePos), comment13));
        
        //Material Loading Bar Static
        String comment14 = "Path to materials loading bar";
        GTprogress = cfg.getString("materialProgressBar", "layout", GTprogress, comment14);
        String comment15 = "Material loading bar position";
        GTprogressPos = stringToIntArray(cfg.getString("GTProgressBarPos", "layout", intArrayToString(GTprogressPos), comment15));
        //Material Loading Bar Animated
        String comment16 = "Path to animated materials loading bar";
        GTprogressAnimated = cfg.getString("materialProgressBarAnimated", "layout", GTprogress, comment16);
        String comment17 = "Material animated loading bar position";
        GTprogressPosAnimated = stringToIntArray(cfg.getString("GTProgressBarPosAnimated", "layout", intArrayToString(GTprogressPosAnimated), comment17));
        //Material Loading Bar Text
        String comment18 = "Main loading bar text position. The two values are for positon (x and y).";
        GTprogressTextPos = stringToIntArray(cfg.getString("materialProgressBarTextPos", "layout", intArrayToString(GTprogressTextPos), comment18));
        //Main Loading Bar Percentage
        String comment19 = "Main loading bar percentage position";
        GTprogressPercentagePos = stringToIntArray(cfg.getString("materialProgressBarPercentagePos", "layout", intArrayToString(GTprogressPercentagePos), comment19));
        
        //Some text properties
        String comment20 = "Whether the text should be rendered with a shadow. Recommended, unless the background is really dark";
        textShadow = Boolean.parseBoolean(cfg.getString("textShadow", "layout", String.valueOf(textShadow), comment20));
        String comment21 = "Color of text in hexadecimal format";
        textColor = cfg.getString("textColor", "layout", textColor, comment21);
        
        //Stuff related to random backgrounds
        String comment22 = "Whether display a random background from the random backgrounds list";
        randomBackgrounds = Boolean.parseBoolean(cfg.getString("randomBackgrounds", "layout", String.valueOf(randomBackgrounds), comment22));
        String comment23 = "List of paths to backgrounds that will be used if randomBackgrounds is true."+System.lineSeparator()+
        		"The paths must be separated by commas."+System.lineSeparator();
        randomBackgroundArray = parseBackgroundCFGListToArray((cfg.getString("backgroundList", "layout", parseBackgroundArraytoCFGList(randomBackgroundArray), comment23)));
        if (randomBackgrounds) {
        	Random rand = new Random();
			background = randomBackgroundArray[rand.nextInt(randomBackgroundArray.length)];
		}
        
        // Add ourselves as a resource pack
        if (!preview) {
            if (!ProgressDisplayer.coreModLocation.isDirectory())
                myPack = new FMLFileResourcePack(ProgressDisplayer.modContainer);
            else
                myPack = new FMLFolderResourcePack(ProgressDisplayer.modContainer);
            getOnlyList().add(myPack);

            mc.refreshResources();
        }
        // Open the special config directory
        //File configDir = new File("./config/BetterLoadingScreen");
        File configDir = new File("./config");
        /*if (!configDir.exists()) {
            configDir.mkdirs();
        }*/
    }

    @Override
    public void displayProgress(String text, float percent) {
    	if (alexiil.mods.load.MinecraftDisplayer.isRegisteringGTmaterials || isReplacingVanillaMaterials || isRegisteringBartWorks) {
    		images = new ImageRender[11];
            
    		if (!background.equals("")) {
    			images[0] = new ImageRender(background, EPosition.TOP_LEFT, EType.STATIC, new Area(0, 0, 256, 256), new Area(0, 0, 0, 0));
			} else {
				images[0] = new ImageRender("betterloadingscreen:textures/transparent.png", EPosition.TOP_LEFT, EType.STATIC, new Area(0, 0, 256, 256), new Area(0, 0, 10, 10));
			} 
    		if (!title.equals("")) {
				images[1] = new ImageRender(title, EPosition.CENTER, EType.STATIC, new Area(titlePos[0], titlePos[1], titlePos[2], titlePos[3]), new Area(titlePos[4], titlePos[5], titlePos[6], titlePos[7]));
			} else {
				images[1] = new ImageRender("betterloadingscreen:textures/transparent.png", EPosition.TOP_LEFT, EType.STATIC, new Area(0, 0, 256, 256), new Area(0, 0, 10, 10));
			}
            images[2] = new ImageRender(fontTexture, EPosition.CENTER, EType.DYNAMIC_TEXT_STATUS, null, new Area(gtptp[0], gtptp[1], 0, 0), "ffffff", null, "");
            images[3] = new ImageRender(fontTexture, EPosition.CENTER, EType.DYNAMIC_TEXT_PERCENTAGE, null, new Area(GTprogressPercentagePos[0], GTprogressPercentagePos[1], 0, 0), "ffffff", null, "");
            //progressbars
            images[4] = new ImageRender(progress, EPosition.CENTER, EType.STATIC, new Area(progressPos[0], progressPos[1], progressPos[2], progressPos[3]), new Area(progressPos[4], progressPos[5], progressPos[6], progressPos[7]));
            images[5] = new ImageRender(progress, EPosition.CENTER, EType.DYNAMIC_PERCENTAGE, new Area(progressPosAnimated[0], progressPosAnimated[1], progressPosAnimated[2], progressPosAnimated[3]), new Area(progressPosAnimated[4], progressPosAnimated[5], progressPosAnimated[6], progressPosAnimated[7]));
            
            ///GT
            images[6] = new ImageRender(fontTexture, EPosition.CENTER, EType.DYNAMIC_TEXT_STATUS, null, new Area(progressTextPos[0], progressTextPos[1], 0, 0), "ffffff", null, "");
            images[7] = new ImageRender(fontTexture, EPosition.CENTER, EType.DYNAMIC_TEXT_PERCENTAGE, null, new Area(progressPercentagePos[0], progressPercentagePos[1], 0, 0), "ffffff", null, "");
            //progressbars
            images[8] = new ImageRender(GTprogress, EPosition.CENTER, EType.STATIC, new Area(GTprogressPos[0], GTprogressPos[1], GTprogressPos[2], GTprogressPos[3]), new Area(GTprogressPos[4], GTprogressPos[5], GTprogressPos[6], GTprogressPos[7]));
            images[9] = new ImageRender(GTprogress, EPosition.CENTER, EType.DYNAMIC_PERCENTAGE, new Area(GTprogressPosAnimated[0], GTprogressPosAnimated[1], GTprogressPosAnimated[2], GTprogressPosAnimated[3]), new Area(GTprogressPosAnimated[4], GTprogressPosAnimated[5], GTprogressPosAnimated[6], GTprogressPosAnimated[7]));
            ///

            images[10] = new ImageRender(null, null, EType.CLEAR_COLOUR, null, null, "ffffff", null, "");
            //
		}	else {
			images = new ImageRender[7];
			if (!background.equals("")) {
    			images[0] = new ImageRender(background, EPosition.TOP_LEFT, EType.STATIC, new Area(0, 0, 256, 256), new Area(0, 0, 0, 0));
			} else {
				images[0] = new ImageRender("betterloadingscreen:textures/transparent.png", EPosition.TOP_LEFT, EType.STATIC, new Area(0, 0, 256, 256), new Area(0, 0, 10, 10));
			}
			if (!title.equals("")) {
				images[1] = new ImageRender(title, EPosition.CENTER, EType.STATIC, new Area(titlePos[0], titlePos[1], titlePos[2], titlePos[3]), new Area(titlePos[4], titlePos[5], titlePos[6], titlePos[7]));
			} else {
				images[1] = new ImageRender("betterloadingscreen:textures/transparent.png", EPosition.TOP_LEFT, EType.STATIC, new Area(0, 0, 256, 256), new Area(0, 0, 10, 10));
			}
            images[2] = new ImageRender(fontTexture, EPosition.CENTER, EType.DYNAMIC_TEXT_STATUS, null, new Area(progressTextPos[0], progressTextPos[1], 0, 0), "ffffff", null, "");
            images[3] = new ImageRender(fontTexture, EPosition.CENTER, EType.DYNAMIC_TEXT_PERCENTAGE, null, new Area(progressPercentagePos[0], progressPercentagePos[1], 0, 0), "ffffff", null, "");
            images[4] = new ImageRender(progress, EPosition.CENTER, EType.STATIC, new Area(progressPos[0], progressPos[1], progressPos[2], progressPos[3]), new Area(progressPos[4], progressPos[5], progressPos[6], progressPos[7]));
            images[5] = new ImageRender(progress, EPosition.CENTER, EType.DYNAMIC_PERCENTAGE, new Area(progressPosAnimated[0], progressPosAnimated[1], progressPosAnimated[2], progressPosAnimated[3]), new Area(progressPosAnimated[4], progressPosAnimated[5], progressPosAnimated[6], progressPosAnimated[7]));
            images[6] = new ImageRender(null, null, EType.CLEAR_COLOUR, null, null, "ffffff", null, "");
		}
    	
        resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        preDisplayScreen();
        
        int imageCounter = 0;
        
        if (!isRegisteringGTmaterials && !isReplacingVanillaMaterials && !isRegisteringBartWorks) {
			lastPercent = percent;
		}
        for (ImageRender image : images) {
//        	if (!usingGT) {
//				lastPercent = percent;
//			}
            if (image != null && !((isRegisteringGTmaterials || isReplacingVanillaMaterials || isRegisteringBartWorks) && imageCounter > 4 && (isRegisteringGTmaterials || isReplacingVanillaMaterials || isRegisteringBartWorks) && imageCounter < 9)) {
                drawImageRender(image, text, percent);
            } else if (image != null && isRegisteringGTmaterials && !isNice) {
            	drawImageRender(image," Post Initialization: Registering Gregtech materials", lastPercent);
            	
			} else if (image != null && isRegisteringGTmaterials && isNice) {
            	drawImageRender(image," Post Initialization: Registering nice Gregtech materials", lastPercent);
            	if(!hasSaidNice) {
            		hasSaidNice = true;
            		log = LogManager.getLogger("betterloadingscreen");
            		log.info("Yeah, that's nice, funni number");
            	}
			} else if (isReplacingVanillaMaterials) {
				drawImageRender(image," Post Initialization: Gregtech replacing Vanilla materials in recipes", lastPercent);
			} else if (isRegisteringBartWorks) {
				drawImageRender(image," Post Initialization: Registering BartWorks materials", lastPercent);
			}
            imageCounter++;
        }

        postDisplayScreen();

        if (callAgain) {
            // For some reason, calling this again makes pre-init render properly. I have no idea why, it just does
            callAgain = false;
            displayProgress(text, percent);
        }
    }

    private FontRenderer fontRenderer(String fontTexture) {
        if (fontRenderers.containsKey(fontTexture)) {
            return fontRenderers.get(fontTexture);
        }
        FontRenderer font = new FontRenderer(mc.gameSettings, new ResourceLocation(fontTexture), textureManager, false);
        font.onResourceManagerReload(mc.getResourceManager());
        if (!preview) {
            mc.refreshResources();
            font.onResourceManagerReload(mc.getResourceManager());
        }
        fontRenderers.put(fontTexture, font);
        return font;
    }

    public void drawImageRender(ImageRender render, String text, double percent) {
        int startX = render.transformX(resolution.getScaledWidth());
        int startY = render.transformY(resolution.getScaledHeight());
        int PWidth = 0;
        int PHeight = 0;
        int intColor = Integer.parseInt(textColor, 16);
        if (render.position != null) {
            PWidth = render.position.width == 0 ? resolution.getScaledWidth() : render.position.width;
            PHeight = render.position.height == 0 ? resolution.getScaledHeight() : render.position.height;
        }
        GL11.glColor3f(render.getRed(), render.getGreen(), render.getBlue());
        switch (render.type) {
            case DYNAMIC_PERCENTAGE: {
                ResourceLocation res = new ResourceLocation(render.resourceLocation);
                textureManager.bindTexture(res);
                double visibleWidth = PWidth * percent;
                double textureWidth = render.texture.width * percent;
                drawRect(startX, startY, visibleWidth, PHeight, render.texture.x, render.texture.y, textureWidth, render.texture.height);
                break;
            }
            case DYNAMIC_TEXT_PERCENTAGE: {
                FontRenderer font = fontRenderer(render.resourceLocation);
                String percentage = (int) (percent * 100) + "%";
                int width = font.getStringWidth(percentage);
                startX = render.positionType.transformX(render.position.x, resolution.getScaledWidth() - width);
                startY = render.positionType.transformY(render.position.y, resolution.getScaledHeight() - font.FONT_HEIGHT);
                if (textShadow) {
                	font.drawStringWithShadow(percentage, startX, startY, /*render.getColour()*/intColor);
				} else {
					drawString(font, percentage, startX, startY, intColor);
				}
                break;
            }
            case DYNAMIC_TEXT_STATUS: {
                FontRenderer font = fontRenderer(render.resourceLocation);
                int width = font.getStringWidth(text);
                startX = render.positionType.transformX(render.position.x, resolution.getScaledWidth() - width);
                startY = render.positionType.transformY(render.position.y, resolution.getScaledHeight() - font.FONT_HEIGHT);
                /*int currentX = startX; //This allows to draw each char separately.
                for (int i = 0; i < text.length(); i++) {
                	//drawString(font., String.valueOf(text.charAt(i)), currentX, startY, render.getColour());
                	drawString(font, String.valueOf(text.charAt(i)), currentX, startY, render.getColour());
                	currentX += font.getCharWidth(text.charAt(i));
                }*/
                if (textShadow) {
                	font.drawStringWithShadow(text, startX, startY, intColor);
                } else {
                	drawString(font, text, startX, startY, intColor);
                }
                break;
            }
            case STATIC_TEXT: {
                FontRenderer font = fontRenderer(render.resourceLocation);
                int width = font.getStringWidth(render.text);
                int startX1 = render.positionType.transformX(render.position.x, resolution.getScaledWidth() - width);
                int startY1 = render.positionType.transformY(render.position.y, resolution.getScaledHeight() - font.FONT_HEIGHT);
                if (textShadow) {
                	font.drawStringWithShadow(render.text, startX1, startY1, intColor);
				} else {
					drawString(font, render.text, startX1, startY1, intColor);
				}
				break;
            }
            case STATIC: {
                ResourceLocation res = new ResourceLocation(render.resourceLocation);
                textureManager.bindTexture(res);
                drawRect(startX, startY, PWidth, PHeight, render.texture.x, render.texture.y, render.texture.width, render.texture.height);
                break;
            }
            case CLEAR_COLOUR:// Ignore this, as its set elsewhere
                break;
        }
    }

    public void drawString(FontRenderer font, String text, int x, int y, int colour) {
        font.drawString(text, x, y, colour);
        GL11.glColor4f(1, 1, 1, 1);
    }

    public void drawRect(double x, double y, double drawnWidth, double drawnHeight, double u, double v, double uWidth, double vHeight) {
        float f = 1 / 256F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + drawnHeight, 0, u * f, (v + vHeight) * f);
        tessellator.addVertexWithUV(x + drawnWidth, y + drawnHeight, 0, (u + uWidth) * f, (v + vHeight) * f);
        tessellator.addVertexWithUV(x + drawnWidth, y, 0, (u + uWidth) * f, v * f);
        tessellator.addVertexWithUV(x, y, 0, u * f, v * f);
        tessellator.draw();
    }

    private void preDisplayScreen() {
        if (textureManager == null) {
            if (preview) {
                textureManager = mc.renderEngine;
            }
            else {
                textureManager = mc.renderEngine = new TextureManager(mc.getResourceManager());
                mc.refreshResources();
                textureManager.onResourceManagerReload(mc.getResourceManager());
                mc.fontRenderer = new FontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), textureManager, false);
                if (mc.gameSettings.language != null) {
                    mc.fontRenderer.setUnicodeFlag(mc.func_152349_b());
                    LanguageManager lm = mc.getLanguageManager();
                    mc.fontRenderer.setBidiFlag(lm.isCurrentLanguageBidirectional());
                }
                mc.fontRenderer.onResourceManagerReload(mc.getResourceManager());
                callAgain = true;
            }
        }
        if (fontRenderer != mc.fontRenderer)
            fontRenderer = mc.fontRenderer;
        // if (textureManager != mc.renderEngine)
        // textureManager = mc.renderEngine;
        resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int i = resolution.getScaleFactor();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, (double) resolution.getScaledWidth(), (double) resolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glClearColor(clearRed, clearGreen, clearBlue, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

        GL11.glColor4f(1, 1, 1, 1);
    }

    public ImageRender[] getImageData() {
        return images;
    }

    private void postDisplayScreen() {
        mc.func_147120_f();
    }

    @Override
    public void close() {
        getOnlyList().remove(myPack);
    }
}
