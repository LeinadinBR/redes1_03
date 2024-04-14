/*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
  Autor: Daniel Nogueira
  Matricula: 201911910
  Inicio...: 20 de Maio de 2022
  Alteracao: 21 de Maio de 2022
  Nome.....: Timer
  Funcao...: Classe que serve com timer dos quadros
  =-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
public class Timer extends Thread {
  private long tempo;
  private int tempoMax;
  private Boolean flag; //em relacao ao ack externo
  private boolean localFlag = false; //Diz respeito a se o timer está on
  private Boolean flagFinalizado;

  public Timer(int tempoMax, Boolean flag, Boolean flagFinalizado){
    this.tempoMax = tempoMax;
    this.flag = flag;
    this.flagFinalizado = flagFinalizado;
  }

  @Override
  public void run(){
    this.tempo = System.currentTimeMillis();
    localFlag = true;
    while (System.currentTimeMillis()-tempo <= tempoMax){
      if (flag)
        break;
    }
    localFlag = false;
  }


  //Getters e Setters
  public long getTempo() {
    return tempo;
  }

  public void setTempo(long tempo) {
    this.tempo = tempo;
  }

  public int getTempoMax() {
    return tempoMax;
  }

  public void setTempoMax(int tempoMax) {
    this.tempoMax = tempoMax;
  }

  public Boolean getFlag() {
    return flag;
  }

  public void setFlag(Boolean flag) {
    this.flag = flag;
  }

  public boolean isLocalFlag() {
    return localFlag;
  }

  public void setLocalFlag(boolean localFlag) {
    this.localFlag = localFlag;
  }

  public Boolean getFlagFinalizado() {
    return flagFinalizado;
  }

  public void setFlagFinalizado(Boolean flagFinalizado) {
    this.flagFinalizado = flagFinalizado;
  }
}
