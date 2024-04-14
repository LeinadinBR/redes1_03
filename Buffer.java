import java.util.LinkedList;

public class Buffer extends Thread{
  private MeioEnlaceDeDados meioEnlaceDeDados1;
  private LinkedList<Quadro> quadros;
  
  public Buffer(MeioEnlaceDeDados meioEnlaceDeDados1){
    this.meioEnlaceDeDados1 = meioEnlaceDeDados1;
    quadros = new LinkedList<>();
  }

  public void adicionarQuadrosNoBuffer(Quadro quadro){
    quadros.add(quadro);
  }

  @Override
  public void run(){
    while (!quadros.isEmpty()){
      for (int i=0; i<quadros.size(); i++){
        if (quadros.get(i).getFlagFinalizado() == false){
          if (quadros.get(i).getFlag()){
            quadros.get(i).iniciarTimer();
            meioEnlaceDeDados1.reenviarQuadro(quadros.get(i), meioEnlaceDeDados1.getTipoCodificacao(), 
            meioEnlaceDeDados1.getTipoDeEnlace(), meioEnlaceDeDados1.getTipoDeEnlace2(), meioEnlaceDeDados1.getTaxaDeErro());
          }
        }
      }
    }
  }

  public LinkedList<Quadro> getQuadros() {
    return quadros;
  }

  public void setQuadros(LinkedList<Quadro> quadros) {
    this.quadros = quadros;
  }
}
