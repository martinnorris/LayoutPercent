package lpc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class LayoutPercent implements LayoutManager
{
	public LayoutPercent()
	{
		m_listPositions = new LayoutList();
		m_compareX = new LayoutCompareX();
		m_compareY = new LayoutCompareY();
	}
	
	class LayoutPosition
	{
		public LayoutPosition(Component component, float fX, float fY, String scName)
		{
			m_fX = fX;
			m_fY = fY;
			m_component = component;
			
			m_iW = 0;
			m_iH = 0;
			
			m_scName = scName;
		}
		
		private float m_fX, m_fY;
		private int m_iW, m_iH;
		private Component m_component;
		private String m_scName;
		
		public void setSizes()
		{
			Dimension dimension = m_component.getPreferredSize();
			m_iW = dimension.width;
			m_iH = dimension.height;			
		}
		
		public boolean isVisible()
		{
			return m_component.isVisible();
		}
		
		public float getFromX()
		{
			return m_fX;
		}
		
		public float getFromY()
		{
			return m_fY;
		}
		
		public int getNextX()
		{
			return m_iW;
		}
		
		public int getNextY()
		{
			return m_iH;
		}
		
		public int getSizeX()
		{
			// Scale the width by the constraint so iX 5% and width 100
			return m_iW;
		}
		
		public int getSizeY()
		{
			// Scale the width by the constraint so iX 5% and width 100
			return m_iH;
		}

		public void setBounds(int iFromX, int iWidth, int iFromY, int iHeight) 
		{
			Dimension dimension = m_component.getPreferredSize();
			m_iW = dimension.width;
			m_iH = dimension.height;
			
			int iX = setBoundsX(iFromX, m_fX, m_iW, iWidth);

			int iY = setBoundsY(iFromY, m_fY, m_iH, iHeight);
			
			m_component.setBounds(iX, iY, m_iW, m_iH);
		}
		
		protected int setBoundsX(int iFromX, float fX, int iW, int iWidth)
		{
			float fXPC = fX/100;
			return iFromX + (int)(fXPC * (iWidth-iW));
		}
		
		protected int setBoundsY(int iFromY, float fY, int iH, int iHeight)
		{
			float fYPC = fY/100;
			return iFromY + (int)(fYPC * (iHeight-iH));
		}
		
		@Override
		public String toString()
		{
			return m_scName;
		}
	}
	
	class LayoutPositionLeftTop extends LayoutPosition
	{
		public LayoutPositionLeftTop(Component component, float fX, float fY, String scName)
		{
			super(component, fX, fY, scName);
		}
		
		@Override
		protected int setBoundsX(int iFromX, float fX, int iW, int iWidth)
		{
			float fXPC = fX/100;
			return iFromX + (int)(fXPC * iWidth);
		}
		
		@Override
		protected int setBoundsY(int iFromY, float fY, int iH, int iHeight)
		{
			float fYPC = fY/100;
			return iFromY + (int)(fYPC * iHeight);
		}
		
	}
	
	class LayoutList extends ArrayList<LayoutPosition>
	{
		private static final long serialVersionUID = -4321295422861038012L;
		
		@Override
		public boolean remove(Object object)
		{
			ListIterator<LayoutPosition> iterateList = listIterator();
			
			while (iterateList.hasNext())
			{
				LayoutPosition position = iterateList.next();
				if (position.m_component==object) return super.remove(position);
			}
			return false;
		}
	}
	
	class LayoutCompareX implements Comparator<LayoutPosition>
	{
		@Override
		public int compare(LayoutPosition o1, LayoutPosition o2) 
		{
			if (o1.m_fX>o2.m_fX) return 1;
			if (o1.m_fX<o2.m_fX) return -1;
			return 0;
		}
	}
	
	class LayoutCompareY implements Comparator<LayoutPosition>
	{
		@Override
		public int compare(LayoutPosition o1, LayoutPosition o2) 
		{
			if (o1.m_fY>o2.m_fY) return 1;
			if (o1.m_fY<o2.m_fY) return -1;
			return 0;
		}
	}
	
	private LayoutList m_listPositions;
	private LayoutCompareX m_compareX;
	private LayoutCompareY m_compareY;
	
	private Dimension getSmallest(Container container)
	{
		Insets insets = container.getInsets();

		ListIterator<LayoutPosition> iterateListX = m_listPositions.listIterator();
		
		int iTotalX = 0;
		
		while (iterateListX.hasNext())
		{
			LayoutPosition positionCheck = iterateListX.next();
			if (!positionCheck.isVisible()) continue;
			if (positionCheck.m_iW>iTotalX) iTotalX = positionCheck.m_iW;
		}
		
		ListIterator<LayoutPosition> iterateListY = m_listPositions.listIterator();
		
		int iTotalY = 0;
		
		while (iterateListY.hasNext())
		{
			LayoutPosition positionCheck = iterateListY.next();
			if (!positionCheck.isVisible()) continue;
			if (positionCheck.m_iH>iTotalY) iTotalY = positionCheck.m_iH;
		}
		
		return new Dimension(iTotalX + insets.left + insets.right, iTotalY + insets.top + insets.bottom);		
	}
	
	@SuppressWarnings("unused") // TODO calculate optimum size
	private Dimension setSizes(Container container)
	{
		// Find X size by
		
		// ... sort by Y position
		Collections.sort(m_listPositions, m_compareY);
		ListIterator<LayoutPosition> iterateListX = m_listPositions.listIterator();
		// ... accumulating maximum size
		float fTotalX = 0;
		for (float fCheckY = 0; iterateListX.hasNext(); )
		{
			LayoutPosition positionCheck = iterateListX.next();
			if (!positionCheck.isVisible()) continue;
			fCheckY = positionCheck.getFromY();
			float fNextY = positionCheck.getNextY();
			// ... and then the size of all components on the same line
			float fTotalXForCheck = getSizesX(positionCheck, fCheckY, fNextY);
			if (fTotalXForCheck>fTotalX) fTotalX = fTotalXForCheck;
			fCheckY = fNextY;
		}

		Collections.sort(m_listPositions, m_compareX);
		ListIterator<LayoutPosition> iterateListY = m_listPositions.listIterator();
		// ... accumulating maximum size
		float fTotalY = 0;
		for (float fCheckX = 0; iterateListY.hasNext(); )
		{
			LayoutPosition positionCheck = iterateListY.next();
			if (!positionCheck.isVisible()) continue;
			fCheckX = positionCheck.getFromX();
			float fNextX = positionCheck.getNextX();
			// ... and then the size of all components on the same line
			float fTotalYForCheck = getSizesY(positionCheck, fCheckX, fNextX);
			if (fTotalYForCheck>fTotalY) fTotalY = fTotalYForCheck;
			fCheckX = fNextX;
		}
		
		return new Dimension((int)fTotalX, (int)fTotalY);
	}
	
	private float getSizesX(LayoutPosition positionListX, float fCheckY, float fNextY)
	{
		ListIterator<LayoutPosition> iterateListX = m_listPositions.listIterator();
		iterateListX.set(positionListX);
		
		// Accumulate X sizes on the same line and also set values for overlapping components
		float fTotalX = positionListX.getSizeX();
		
		while (iterateListX.hasNext())
		{
			LayoutPosition positionCheck = iterateListX.next();
			if (!positionCheck.isVisible()) continue;
			float fPositionY = positionCheck.getFromY();
			if (fPositionY>fNextY) return fTotalX;
			fTotalX += positionCheck.getSizeX();
		}
		return fTotalX;
	}

	private float getSizesY(LayoutPosition positionListX, float fCheckY, float fNextY)
	{
		ListIterator<LayoutPosition> iterateListX = m_listPositions.listIterator();
		iterateListX.set(positionListX);
		
		// Accumulate X sizes on the same line and also set values for overlapping components
		float fTotalX = positionListX.getSizeX();
		
		while (iterateListX.hasNext())
		{
			LayoutPosition positionCheck = iterateListX.next();
			if (!positionCheck.isVisible()) continue;
			float fPositionY = positionCheck.getFromY();
			if (fPositionY>fNextY) return fTotalX;
			fTotalX += positionCheck.getSizeX();
		}
		return fTotalX;
	}

	@Override
	public void addLayoutComponent(String scArguments, Component component) 
	{
		// Add the component position
		int iSeparator = scArguments.indexOf(',');

		float fX = 0, fY = 0;
		
		try
		{
			if (0>iSeparator)
			{
				fX = Float.parseFloat(scArguments);
			}
			else
			{
				fX = Float.parseFloat(scArguments.substring(0, iSeparator));
				fY = Float.parseFloat(scArguments.substring(iSeparator+1));
			}
		}
		catch (NumberFormatException x)
		{
			// No position given
			fY = m_listPositions.size() * 5;
		}
		
		fX = Math.min(fX, 100);
		fY = Math.min(fY, 100);
		
		LayoutPosition position = new LayoutPositionLeftTop(component, fX, fY, scArguments);
		position.setSizes();
		m_listPositions.add(position);
		
		return;
	}

	@Override
	public void layoutContainer(Container container) 
	{
		Insets insets = container.getInsets();
		int iParentX = container.getWidth() - (insets.left + insets.right);
		int iParentY = container.getHeight() - (insets.top + insets.bottom);
		
		ListIterator<LayoutPosition> iterateList = m_listPositions.listIterator();
		while (iterateList.hasNext())
		{
			LayoutPosition positionCheck = iterateList.next();
			if (!positionCheck.isVisible()) continue;
			
			positionCheck.setBounds(insets.left, iParentX, insets.top, iParentY);
		}
		
		return;
	}

	@Override
	public Dimension minimumLayoutSize(Container container) 
	{
		return getSmallest(container);
	}

	@Override
	public Dimension preferredLayoutSize(Container container) 
	{
		// TODO Auto-generated method stub
		return new Dimension(400, 600);
	}

	@Override
	public void removeLayoutComponent(Component component) 
	{
		m_listPositions.remove(component);
	}

	public static void frameTest(final LayoutPercent layout, final BufferedImage imageLoaded)
	{
		final JPanel panelTest = new JPanel()
		{
			private static final long serialVersionUID = -3083235267155325363L;
			
			private double m_dfScale = 1.0;
			
			@Override
			public void paintComponent(Graphics g) 
			{
				super.paintComponent(g);
				
				int iScaleX = (int)(m_dfScale * imageLoaded.getWidth());
				int iScaleY = (int)(m_dfScale * imageLoaded.getHeight());
				
				g.drawImage(imageLoaded, 0, 0, iScaleX, iScaleY, null);
				
				return;
			}
			
			public Dimension getPreferredSize()
			{
				int iScaleX = (int)(m_dfScale * imageLoaded.getWidth());
				int iScaleY = (int)(m_dfScale * imageLoaded.getHeight());
				return new Dimension(iScaleX, iScaleY);
			}
		};
		
		Runnable startFrame = new Runnable()
		{
			int iRandomRange(int iRange)
			{
				return (int)(Math.random() * iRange);
			}
			
			@Override
			public void run()
			{
				panelTest.setLayout(layout);
				panelTest.setBorder(BorderFactory.createLineBorder(Color.RED, 20));
				
				String scContent = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
				int iCount = iRandomRange(5)+5;
				
				for (int iIndex = 0; iIndex<iCount; ++iIndex)
				{
					int iSize = iRandomRange(21)+4;
					int iStart = iRandomRange(25-iSize);
					JLabel label = new JLabel(scContent.substring(iStart, iStart+iSize));
					int iX = iRandomRange(90)+5;
					int iY = iRandomRange(90)+5;
					String scConstraint = String.format("%d,%d", iX, iY);
					panelTest.add(label, scConstraint);
				}
				
				String[][] aascPlaces = 
					{
							{"0,0", "50,5", "95,5"},
							{"5,50", "50,50", "95,50"},
							{"5,95", "50,95", "100,100"},
					};
				
				for (int iIndex = 0; iIndex<9; ++iIndex)
				{
					JLabel label = new JLabel(aascPlaces[iIndex % 3][iIndex / 3]);
					panelTest.add(label, aascPlaces[iIndex % 3][iIndex / 3]);
				}
				
				JScrollPane paneScroll = new JScrollPane(panelTest);
				paneScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				paneScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				paneScroll.setPreferredSize(new Dimension(400, 400));
				
				JFrame frame = new JFrame("Test layout");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				
				frame.add(paneScroll);
				
				frame.pack();				
				frame.setVisible(true);
			}
		};
		
		SwingUtilities.invokeLater(startFrame);

		return;
	}
	
	@Override
	public String toString()
	{
		return "LayoutPercent";
	}
	
	public static void main(String[] args) throws IOException 
	{	
		// Relative to class file binary execute
		File fileImage = new File("../../../CharGenX/Tests/Test-Background3.jpg");
		BufferedImage imageLoaded = ImageIO.read(fileImage);
		
		LayoutPercent layout = new LayoutPercent();
		LayoutPercent.frameTest(layout, imageLoaded);
	}
}
