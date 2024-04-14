import java.util.Random;

/*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
  Autor: Daniel Nogueira
  Matricula: 201911910
  Inicio...: 03 de Agosto de 2021
  Alteracao: 21 de Maio de 2022
  Nome.....: MeioComunicacao
  Funcao...: Classe que serve para simular o meio da comunicacao
  =-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
public class MeioTransmicao {
  private MeioFisico meioFisico1, meioFisico2;  //referencias aos meios fisicos que essa classe conecta
  private Controlador controle;                 //referencia ao controlador de animacao
  private Random random = new Random();

  /* *********************
  * Metodo: MeioComunicacao
  * Funcao: Construtor
  * Parametros: MeioFisico meioFisico1, MeioFisico meioFisico2, Controlador controle
  ********************* */
  public MeioTransmicao(MeioFisico meioFisico1, MeioFisico meioFisico2, Controlador controle){
    this.meioFisico1 = meioFisico1;
    this.meioFisico1.setMeioTransmicao(this);
    this.meioFisico2 = meioFisico2;
    this.meioFisico2.setMeioTransmicao(this);
    this.controle = controle;
  }

  /* *********************
  * Metodo: transmitir
  * Funcao: Funcao que passa os bits de um MeioFisico para o outro
  * Parametros: int tipoCOdificacao
  * Retorno: void
  ********************* */
  public void transmitir(int[] bits, int tipoDeCodificacao, int tipoDeEnlace, int tipoDeEnlace2, boolean direcao, int taxaDeErro){
    int erro = random.nextInt(10);
    if (erro < taxaDeErro){
      erro = random.nextInt(bits.length);
      if (bits[erro] == 1)
        bits[erro] = 0;
      else 
        bits[erro] = 1;
    }
    if (direcao){
      controle.getTextField().setText("");
      for (int i=0; i<bits.length; i++){
        if (i==0)
          visualizarTransmissao(0, bits[i]);
        else 
          visualizarTransmissao(bits[i-1], bits[i]);
      }
      if (meioFisico1.isViolacaoTrue()){
        meioFisico2.setViolacaoTrue(true);
        meioFisico1.setViolacaoTrue(false);
      } 
      //passa para a proxima etapa do envio da mensagem
      meioFisico2.decodificaoEspecifica(bits, tipoDeCodificacao, tipoDeEnlace, tipoDeEnlace2);
    }
    else {
      if (meioFisico2.isViolacaoTrue()){
        meioFisico1.setViolacaoTrue(true);
        meioFisico2.setViolacaoTrue(false);
      } 
      //passa para a proxima etapa do envio da mensagem
      meioFisico1.decodificaoEspecifica(bits, tipoDeCodificacao, tipoDeEnlace, tipoDeEnlace2);
    }    
  }

  /* *********************
  * Metodo: visualizarTransmissao
  * Funcao: Funcao que atualiza a caixa de texto
  * Parametros: int i
  * Retorno: void
  ********************* */
  public void visualizarTransmissao(int i,int j) {
    try {
      Thread.sleep(10);
    } catch (InterruptedException e){}


    if (i==0){
      if (j==0){
        preencherTextArea("_");
      }
      else {
        preencherTextArea("|");
        preencherTextArea("¯");
      }
    }
    else {
      if (j==0){
        preencherTextArea("|");
        preencherTextArea("_");
      }
      else {
        preencherTextArea("¯");
      }
    }
  }

  /* *********************
  * Metodo: preencherTextArea
  * Funcao: Concatena uma string no textField
  * Parametros: String s
  * Retorno: void
  ********************* */
  public void preencherTextArea(String s){
    controle.getTextField().setText(controle.getTextField().getText().concat(s));
  }
}
