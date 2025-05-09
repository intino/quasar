package io.quassar.editor.box.util;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Random;

public class LanguageLogoGenerator {
	public static final int WIDTH = 100;
	public static final int HEIGHT = 100;

	private final BufferedImage image;
	private final Graphics2D graphics;
	private final static Random random = new Random();

	public LanguageLogoGenerator() {
		this.image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		this.graphics = init(image.createGraphics());
	}

	private Graphics2D init(Graphics2D graphics) {
		graphics.setComposite(AlphaComposite.Clear);
		graphics.fillRect(0, 0, WIDTH, HEIGHT);
		graphics.setComposite(AlphaComposite.SrcOver);
		graphics.setColor(Color.BLACK);
		DecorationFactory.random().decorate(graphics);
		return graphics;
	}

	public LanguageLogoGenerator put(char letter) {
		GlyphVector gv = glyphVector(letter);
		Rectangle bounds = gv.getPixelBounds(null, 0, 0);
		int x = (WIDTH - bounds.width) / 2 - bounds.x;
		int y = (HEIGHT - bounds.height) / 2 - bounds.y;
		graphics.drawGlyphVector(gv, x, y);
		return this;
	}

	private GlyphVector glyphVector(char letter) {
		Font font = randomFont();
		graphics.setFont(font);
		FontRenderContext frc = graphics.getFontRenderContext();
		return font.createGlyphVector(frc, String.valueOf(letter));
	}

	public BufferedImage image() {
		graphics.dispose();
		return image;
	}

	public static final String[] fontNames = """
			AtomicAge-Regular
			Audiowide-Regular
			Baumans-Regular
			BlackOpsOne-Regular
			BrunoAce-Regular
			Codystar-Regular
			Geostar-Regular
			GeostarFill-Regular
			Mali-Bold
			Offside-Regular
			Orbitron-ExtraBold
			PathwayExtreme_72pt-Black
			Plaster-Regular
			RedRose-Bold
			Revalia-Regular
			Rowdies-Bold
			RussoOne-Regular
			Rye-Regular
			SairaStencilOne-Regular
			SometypeMono-Bold
			StalinistOne-Regular
			Syncopate-Bold
			Tektur-Black
			Tourney_Expanded-Black
			Trocchi-Regular
			Unbounded-Black
			Wallpoet-Regular
			ZenTokyoZoo-Regular
			""".split("\n");

	private static String randomFontName() {
		return fontNames[random.nextInt(fontNames.length)];
	}

	private Font randomFont() {
		return load(randomFontName());
	}

	public Font load(String name) {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("generators/dsl-icon-generator/" + name + ".ttf")) {
			assert is != null;
			Font font = Font.createFont(Font.TRUETYPE_FONT, is);
			return font.deriveFont((float) 80);
		} catch (Exception e) {
			System.out.println("not found: " + name);
			return new Font("SansSerif", Font.BOLD, 80); // fallback
		}
	}

	private static class DecorationFactory {

		static final int TOP = 0;
		static final int BOTTOM = 1;
		static final int TOP_BOTTOM = 2;
		static final int U_SHAPE = 3;
		static final int C_SHAPE = 4;
		static final int BOX = 5;

		static Decoration random() {
			return switch (random.nextInt(8)) {
				case TOP -> DecorationFactory::top;
				case BOTTOM -> DecorationFactory::bottom;
				case TOP_BOTTOM -> DecorationFactory::fit;
				case BOX -> DecorationFactory::box;
				case U_SHAPE -> DecorationFactory::uShape;
				case C_SHAPE -> DecorationFactory::cShape;
				default -> g -> {};
			};
		}

		private static void top(Graphics2D g) {
			g.fillRect(0, 0, WIDTH, randomSize());
		}

		private static void bottom(Graphics2D g) {
			int size = randomSize();
			g.fillRect(0, bottom(size), WIDTH, size);
		}

		private static void fit(Graphics2D g) {
			int size = randomSize();
			g.fillRect(0, 0, WIDTH, size);
			g.fillRect(0, bottom(size), WIDTH, size);
		}

		private static int bottom(int size) {
			return HEIGHT - size;
		}

		private static int right(int size) {
			return WIDTH - size;
		}

		private static void box(Graphics2D g) {
			int size = randomSize();
			g.fillRect(0, 0, WIDTH, size);
			g.fillRect(0, bottom(size), WIDTH, size);
			g.fillRect(0, 0, size, HEIGHT);
			g.fillRect(right(size), 0, size, HEIGHT);
		}

		private static void uShape(Graphics2D g) {
			int size = randomSize();
			g.fillRect(0, bottom(size), WIDTH, size);
			g.fillRect(0, 0, size, HEIGHT);
			g.fillRect(right(size), 0, size, HEIGHT);
		}

		private static void cShape(Graphics2D g) {
			int size = randomSize();
			g.fillRect(0, 0, WIDTH, size);
			g.fillRect(0, 0, size, HEIGHT);
			g.fillRect(0, bottom(size), WIDTH, size);
		}

		static int randomSize() {
			return random.nextInt(8) + 6;
		}
	}
	public interface Decoration {

		void decorate(Graphics2D graphics);
	}
}
