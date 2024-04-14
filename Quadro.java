/*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
  Autor: Daniel Nogueira
  Matricula: 201911910
  Inicio...: 20 de Maio de 2022
  Alteracao: 21 de Maio de 2022
  Nome.....: Quadro
  Funcao...: Classe que serve para representar os quadros
  =-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
public class Quadro {
  private int[] bits;
  private Timer timer;
  private Boolean flag = false; //so eh true se o ack chegar
  private Boolean flagFinalizado = false; //avisa que o timer morreu

  public Quadro(int[] bits){
    this.bits = bits;
    this.timer = new Timer(1000, flag, flagFinalizado);
  }

  
  public void iniciarTimer(){
    timer.start();
  }

  //Getters e Setters
  public int[] getBits() {
    return bits;
  }

  public void setBits(int[] bits) {
    this.bits = bits;
  }

  public Timer getTimer() {
    return timer;
  }

  public void setTimer(Timer timer) {
    this.timer = timer;
  }

  public Boolean getFlag() {
    return flag;
  }

  public void setFlag(Boolean flag) {
    this.flag = flag;
  }

  public Boolean getFlagFinalizado() {
    return flagFinalizado;
  }

  public void setFlagFinalizado(Boolean flagFinalizado) {
    this.flagFinalizado = flagFinalizado;
  }
}
