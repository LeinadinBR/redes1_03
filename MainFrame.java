import java.awt.Dimension;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;

/*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
  Autor: Daniel Nogueira
  Matricula: 201911910
  Inicio...: 30 de Julho de 2021
  Alteracao: 28 de Marco de 2022
  Nome.....: MainFrame
  Funcao...: Classe que serve de frame principal do programa
  =-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
public class MainFrame extends JFrame {
  private Display display;                //objeto de display
  private PanelSimulacao panelSimulacao;  //painel onde a animacao de simulacao acontece
  private PanelTexto panelTexto;          //painel onde os textos e botaos estao
  private MeioTransmicao meioTransmicao;  //simulacao do meio de comunicacao
  private MeioFisico meioFisico1, meioFisico2;  //simulacao do meio fisico
  private MeioEnlaceDeDados meioEnlaceDeDados1, meioEnlaceDeDados2; //
  private MeioAplicacao meioAplicacao1, meioAplicacao2;  //simulacao do meio de aplicacao
  private Controlador controle;                                           //objeto de controle
  private JTextField textField;                                           //textfield onde aparece os sinais
  private JMenuBar menuBar;                                               //barra de menu
  private JMenu camadaFisicaMenu, camadaEnquadramentoMenu, camadaErroMenu;//itens do menu
  private JRadioButtonMenuItem mBinario, mManchester, mDifManchester;     //itens de um menu especifico
  private JRadioButtonMenuItem m1ContagemCaractere, m1InsercaoBytes, m1InsercaoBits, m1ViolacaoFisica;  //itens de um menu especifico
  private JRadioButtonMenuItem m2BPP, m2BPI, m2CRC, m2CDH;
  private int taxaDeErro = 0;

  /* *********************
  * Metodo: MainFrame
  * Funcao: construtor
  * Parametros: nenhum
  ********************* */
  public MainFrame(){
    inicializar();

    menuBar.add(camadaFisicaMenu);
    menuBar.add(camadaEnquadramentoMenu);
    menuBar.add(camadaErroMenu);

    this.setJMenuBar(menuBar);

    this.setLayout(null);
    this.add(panelTexto);
    this.add(textField);
    this.add(panelSimulacao);

    this.setSize(new Dimension(900, 650));
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setResizable(false);
    this.setFocusable(false);
    this.setVisible(true);
    this.setTitle("Whatsapp 3 - Potencia Maxima");
    this.setLocationRelativeTo(null);

    acaoDoTextField();
    acaoDoSlider();
    renderizar();
  }

  /* *********************
  * Metodo: inicializar
  * Funcao: inicializa os objetos da classe
  * Parametros: nenhum
  * Retorno: void
  ********************* */
  private void inicializar(){
    textField = new JTextField();
    textField.setBounds(100, 500, 700, 60);
    textField.setFont(new Font("Arial", Font.BOLD, 20));
    textField.setVisible(false);

    controle = new Controlador(textField);

    display = new Display();
    panelTexto = new PanelTexto();
    panelTexto.setBounds(0, 0, 900, 50);
    panelSimulacao = new PanelSimulacao(display, panelTexto, controle);
    panelSimulacao.setBounds(0, 5, 900, 600);

    controle.setDir(panelSimulacao.getBarrinhaDir());
    controle.setEsq(panelSimulacao.getBarrinhaEsq());

    meioAplicacao1 = new MeioAplicacao(panelTexto.getTexto1(), controle);
    meioAplicacao2 = new MeioAplicacao(panelTexto.getTexto2(), controle);
    meioEnlaceDeDados1 = new MeioEnlaceDeDados(meioAplicacao1, controle);
    meioEnlaceDeDados2 = new MeioEnlaceDeDados(meioAplicacao2, controle);
    meioFisico1 = new MeioFisico(meioEnlaceDeDados1, controle);
    meioFisico2 = new MeioFisico(meioEnlaceDeDados2, controle);
    meioTransmicao = new MeioTransmicao(meioFisico1, meioFisico2, controle);

    menuBar = new JMenuBar();
  
    camadaFisicaMenu = new JMenu("C. Fisica");
    camadaEnquadramentoMenu = new JMenu("C. Enquadramento");
    camadaErroMenu = new JMenu("C. Erro");

    mBinario = new JRadioButtonMenuItem("Binario");
    mBinario.setSelected(true);
    mManchester = new JRadioButtonMenuItem("Manchester");
    mDifManchester = new JRadioButtonMenuItem("Dif. Manchester");
    m1ContagemCaractere = new JRadioButtonMenuItem("C. Caracteres");
    m1ContagemCaractere.setSelected(true);
    m1InsercaoBytes = new JRadioButtonMenuItem("Insercao Bytes");
    m1InsercaoBits = new JRadioButtonMenuItem("Insercao Bits");
    m1ViolacaoFisica = new JRadioButtonMenuItem("Violacao C. Fisica");
    m2BPP = new JRadioButtonMenuItem("Bit Paridade Par");
    m2BPP.setSelected(true);
    m2BPI = new JRadioButtonMenuItem("Bit Paridade Impar");
    m2CRC = new JRadioButtonMenuItem("CRC");
    m2CDH = new JRadioButtonMenuItem("Hamming");

    ButtonGroup bg = new ButtonGroup();
    bg.add(mBinario);
    bg.add(mManchester);
    bg.add(mDifManchester);

    ButtonGroup bg1 = new ButtonGroup();
    bg1.add(m1ContagemCaractere);
    bg1.add(m1InsercaoBytes);
    bg1.add(m1InsercaoBits);
    bg1.add(m1ViolacaoFisica);

    ButtonGroup bg2 = new ButtonGroup();
    bg2.add(m2BPP);
    bg2.add(m2BPI);
    bg2.add(m2CRC);
    bg2.add(m2CDH);

    camadaFisicaMenu.add(mBinario);
    camadaFisicaMenu.add(mManchester);
    camadaFisicaMenu.add(mDifManchester);

    camadaEnquadramentoMenu.add(m1ContagemCaractere);
    camadaEnquadramentoMenu.add(m1InsercaoBytes);
    camadaEnquadramentoMenu.add(m1InsercaoBits);
    camadaEnquadramentoMenu.add(m1ViolacaoFisica);

    camadaErroMenu.add(m2BPP);
    camadaErroMenu.add(m2BPI);
    camadaErroMenu.add(m2CRC);
    camadaErroMenu.add(m2CDH);
  }

  /* *********************
  * Metodo: renderizar
  * Funcao: chama a funcao de renderizar de panelSimulacao
  * Parametros: nenhum
  * Retorno: void
  ********************* */
  private void renderizar(){
    panelSimulacao.renderizar();
  }

  /* *********************
  * Metodo: acaoDoTextField
  * Funcao: da a acao para o botao que envia as mensagens
  * Parametros: nenhum
  * Retorno: void
  ********************* */
  private void acaoDoTextField(){
    panelTexto.getOkBtn().addActionListener(l -> {
      if (panelTexto.getTexto1().getText()!= null){
        controle.setIsProcessing(true);
        meioAplicacao1.codificarParaBits(meioAplicacao1.getCaixaTexto().getText(), selectedIndex(0), selectedIndex(1), selectedIndex(2), taxaDeErro);
        panelTexto.getTexto1().setText("");
      }
    });
  }

  private void acaoDoSlider(){
    panelTexto.getSlider().addChangeListener(l -> {
      this.taxaDeErro = panelTexto.getSlider().getValue();
    });
  }

  /* *********************
  * Metodo: selectedIndex
  * Funcao: Funcao que verifica qual o index do menu esta selecionado baseado em um int que define qual item do menu eh
  * Parametros: int i
  * Retorno: int
  ********************* */
  private int selectedIndex(int i){
    switch (i){
      case 0:
        if (mBinario.isSelected())
          return 0;
        else if (mManchester.isSelected())
          return 1;
        else 
          return 2;
      case 1:
        if (m1ContagemCaractere.isSelected())
          return 0;
        else if (m1InsercaoBytes.isSelected())
          return 1;
        else if (m1InsercaoBits.isSelected())
          return 2;
        else 
          return 3;
      case 2:
        if (m2BPP.isSelected())
          return 0;
        else if (m2BPI.isSelected())
          return 1;
        else if (m2CRC.isSelected())
          return 2;
        else if (m2CDH.isSelected())
          return 3;
    }
    return 0;
  }

}
