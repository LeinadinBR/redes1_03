import java.util.LinkedList;

/*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
  Autor: Daniel Nogueira
  Matricula: 201911910
  Inicio...: 18 de Marco de 2022
  Alteracao: 21 de Maio de 2022
  Nome.....: MeioEnlaceDeDados
  Funcao...: Classe que serve para simular o meio de enlace de dados
  =-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
public class MeioEnlaceDeDados {

  private LinkedList<int[]> bitsArmazenados = new LinkedList<>();       //bits armazenados nessa camada
  private MeioAplicacao meioAplicacao; //referencia ao meio de aplicacao
  private MeioFisico meioFisico;       //referencia ao meio fisico
  private Quadro[] quadros; //quadros no estado de buffer
  private Buffer buffer; //buffer para guardar e reenviar quadros
  private int tipoCodificacao, tipoDeEnlace, tipoDeEnlace2, taxaDeErro; //referencias aos numeros dados

  /* *********************
  * Metodo: MeioEnlaceDeDados
  * Funcao: Construtor
  * Parametros: MeioAplicacao meioAplicacao, Controlador controle
  ********************* */
  public MeioEnlaceDeDados(MeioAplicacao meioAplicacao, Controlador controle){
    this.meioAplicacao = meioAplicacao;
    meioAplicacao.setMeioEnlaceDeDados(this);
    buffer = new Buffer(this);
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosTransmissora
  * Funcao: funcao principal da camada que chama sub-funcoes
  * Parametros: int[] bits, int tipoCodificacao, int tipoDeEnlace
  * Retorno: void
  ********************* */
  public void camadaEnlaceDeDadosTransmissora(int[] bits, int tipoCodificacao, int tipoDeEnlace, int tipoDeEnlace2, int taxaDeErro){
    this.taxaDeErro = taxaDeErro;
    camadaEnlaceDeDadosTransmissoraEnquadramento(bits, tipoDeEnlace);
    for (int i=0; i<bitsArmazenados.size(); i++){
      bitsArmazenados.set(i, camadaEnlaceDeDadosTransmissoraControleDeErro(bitsArmazenados.get(i), tipoDeEnlace2));
    }

    this.quadros = new Quadro[bitsArmazenados.size()];
    
    for (int i=0; i<bitsArmazenados.size(); i++){
      quadros[i] = new Quadro(bitsArmazenados.get(i));
      quadros[i].iniciarTimer();
      buffer.adicionarQuadrosNoBuffer(quadros[i]);
      if (!buffer.isAlive())
        buffer.start();
      meioFisico.codificacaoEspecifica(quadros[i].getBits(), tipoCodificacao, tipoDeEnlace, tipoDeEnlace2, true, taxaDeErro);
    }
  }

  /* *********************
  * Metodo: reenviarQuadro
  * Funcao: funcao que reenvia quadro quando o timer morre
  * Parametros: Quadro quadro, int tipoCodificacao, int tipoDeEnlace, int tipoDeEnlace2, int texaDeErro
  * Retorno: void
  ********************* */
  public void reenviarQuadro(Quadro quadro, int tipoCodificacao, int tipoDeEnlace, int tipoDeEnlace2, int taxaDeErro){
    meioFisico.codificacaoEspecifica(quadro.getBits(), tipoCodificacao, tipoDeEnlace, tipoDeEnlace2, true, taxaDeErro);
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosTransmissoraEnquadramento
  * Funcao: funcao que chama as funcoes de enquadramento
  * Parametros: int[] bits, int tipoDeEnlace
  * Retorno: void
  ********************* */
  public void camadaEnlaceDeDadosTransmissoraEnquadramento(int[] bits, int tipoDeEnlace){   
    switch(tipoDeEnlace){
      case 0: //contagem de caractereres
        bitsArmazenados = camadaEnlaceDeDadosTransmissoraEnquadramentoContagemDeCaracteres(bits);
        break;
      case 1: //insercao de bytes
        bitsArmazenados = camadaEnlaceDeDadosTransmissoraEnquadramentoInsercaoDeBytes(bits);
        break;
      case 2: //insercao de bits
        bitsArmazenados = camadaEnlaceDeDadosTransmissoraEnquadramentoInsercaoDeBits(bits);
        break;
      case 3: //violacao da camada fisica
        bitsArmazenados = camadaEnlaceDeDadosTransmissoraEnquadramentoViolacaoDaCamadaFisica(bits);
        break;
    }
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosTransmissoraEnquadramentoContagemDeCaracteres
  * Funcao: funcao que enquadra por contagem de caracteres
  * Parametros: int[] bits
  * Retorno: LinkedList<int[]>
  ********************* */
  private LinkedList<int[]> camadaEnlaceDeDadosTransmissoraEnquadramentoContagemDeCaracteres(int[] bits){
    int[] subArray = new int[16];

    int count = 0;
    for (int i=0; i<bits.length; i++){
      if (i % 16 == 0)
        count++;
    }

    LinkedList<int[]> arrayFinal = new LinkedList<>();

    count = 0;
    for (int i=0; i<=bits.length; i++){
      if (count == 16){
        subArray = getSubArrray(bits, i-16, i);
        arrayFinal.add(contagemDeCaracteresAlg(subArray));
        count = 0;
      }
      count++;
    }

    return arrayFinal;
  }

  /* *********************
  * Metodo: contagemDeCaracteresAlg
  * Funcao: funcao que realiza o algoritmo de contagem de caracteres
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] contagemDeCaracteresAlg(int[] bits){
    int[] bitsFinal = new int[bits.length + 8];

    int[] tres = {0,0,1,1,0,0,1,1};

    int count = 0;
    for (int i=0; i<bitsFinal.length; i++){
      if (i<8){
        bitsFinal[i] = tres[i];
      }
      else {
        bitsFinal[i] = bits[count];
        count++;
      }
    }
    return bitsFinal;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosTransmissoraEnquadramentoInsercaoDeBytes
  * Funcao: funcao que enquadra por insercao de bytes
  * Parametros: int[] bits
  * Retorno: LinkedList<int[]>
  ********************* */
  private LinkedList<int[]> camadaEnlaceDeDadosTransmissoraEnquadramentoInsercaoDeBytes(int[] bits){
    int[] subArray  = null;

    int count = 0;
    for (int i=0; i<bits.length; i++){
      if (i % 16 == 0)
        count++;
    }

    LinkedList<int[]> arrayFinal = new LinkedList<>();

    count = 0;
    for (int i=0; i<=bits.length; i++){
      if (count == 16){
        subArray = getSubArrray(bits, i-16, i);
        arrayFinal.add(insercaoDeBytesAlg(subArray));
        count = 0;
      }
      count++;
    }

    return arrayFinal;
  }

  /* *********************
  * Metodo: insercaoDeBytesAlg
  * Funcao: funcao que realiza o algoritmo de insercao de bytes
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] insercaoDeBytesAlg(int[] bits){
    int[] esc = {1,1,1,0,0,1,1,1};  //231 em ascii
    int[] flag = {1,1,1,0,1,0,0,0};  //232 em ascii

    int count = 0;
    for (int i=0; i<bits.length; i+=8){
      if (isArrayEqual(getSubArrray(bits, i, i+8), esc) || isArrayEqual(getSubArrray(bits, i, i+8), flag))
        count++;
    }    

    int[] arrayFinal = new int[bits.length + 16 + count*8];
    arrayFinal = adicionarArrayDentroDeArray(flag, arrayFinal, 0);
    arrayFinal = adicionarArrayDentroDeArray(flag, arrayFinal, arrayFinal.length-8);

    count = 0;
    for (int i=8; i<arrayFinal.length-8; i++){
      if (isArrayEqual(getSubArrray(arrayFinal, i, i+8), flag) || isArrayEqual(getSubArrray(arrayFinal, i, i+8), esc)){
        arrayFinal = adicionarArrayDentroDeArray(esc, arrayFinal, i);
        i+=7;
        continue;
      }
      else {
        arrayFinal[i] = bits[count];
        count++;
      }
    }

    return arrayFinal;
  }

  /* *********************
  * Metodo: adicionarArrayDentroDeArray
  * Funcao: funcao que adiciona um array dentro do outro em determinada posicao
  * Parametros: int[] menor, int[] mario, int posicao
  * Retorno: int[]
  ********************* */
  private int[] adicionarArrayDentroDeArray(int[] menor, int[] maior, int posicao){
    int count = 0;
    for (int i=0; i<maior.length; i++){
      if (i >= posicao){
        try {
          maior[i] = menor[count];
          count++; 
        }
        catch (IndexOutOfBoundsException ex){
          break;
        }
      }
    }
    return maior;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosTransmissoraEnquadramentoInsercaoDeBits
  * Funcao: funcao que enquadra por insercao de bits
  * Parametros: int[] bits
  * Retorno: LinkedList<int[]>
  ********************* */
  private LinkedList<int[]> camadaEnlaceDeDadosTransmissoraEnquadramentoInsercaoDeBits(int[] bits){
    int[] subArray  = null;

    int count = 0;
    for (int i=0; i<bits.length; i++){
      if (i % 16 == 0)
        count++;
    }

    LinkedList<int[]> arrayFinal = new LinkedList<>();

    count = 0;
    for (int i=0; i<bits.length; i++){
      if (count == 16){
        subArray = getSubArrray(bits, i-16, i);
        arrayFinal.add(insercaoDeBitsAlg(subArray));
        count = 0;
      }
      count++;
    }

    return arrayFinal;
  }

  /* *********************
  * Metodo: insercaoDeBitsAlg
  * Funcao: funcao que realiza o algoritmo de insercao de bits
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] insercaoDeBitsAlg(int[] bits){
    int[] flag = {0,1,1,1,1,1,1,0}; //6 bits 1 seguidos

    int bitCount = 0;
    for (int i=0; i<bits.length; i++){
      if (cincoUmSeguidos(getSubArrray(bits, i, i+5)))
        bitCount++;
    }  

    int[] bitsFinal = new int[bits.length + bitCount + 16];
    
    bitsFinal = adicionarArrayDentroDeArray(flag, bitsFinal, 0);
    bitsFinal = adicionarArrayDentroDeArray(flag, bitsFinal, bitsFinal.length-8);

    int count = 0;
    for (int i=8; i<bitsFinal.length-8; i++){
      if (cincoUmSeguidos(getSubArrray(bits, count, count+5))){
        bitsFinal[i] = bits[count];
        bitsFinal[i+1] = bits[count+1];
        bitsFinal[i+2] = bits[count+2];
        bitsFinal[i+3] = bits[count+3];
        bitsFinal[i+4] = bits[count+4];
        bitsFinal[i+5] = 0;
        i+=5;
        count+=5;
      }
      else {
        bitsFinal[i] = bits[count];
        count++;
      }
    }

    return bitsFinal;
  }

  /* *********************
  * Metodo: cincoUmSeguidos
  * Funcao: funcao que verifica um array tem 5 1 seguidos
  * Parametros: int[] bits
  * Retorno: boolean
  ********************* */
  private boolean cincoUmSeguidos(int bits[]){
    if (bits[0] == 1 && bits[1] == 1 && bits[2] == 1 && bits[3] == 1 && bits[4] == 1)
      return true;
    return false;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosTransmissoraEnquadramentoViolacaoDaCamadaFisica
  * Funcao: funcao que enquadra por violacao da camada fisica
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  public LinkedList<int[]> camadaEnlaceDeDadosTransmissoraEnquadramentoViolacaoDaCamadaFisica(int[] bits){
    //desisti de fazer por conta do tempo TODO
    return null;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosTransmissoraControleDeErro
  * Funcao: funcao que lida com o controle de erro pela parte transmissora
  * Parametros: int[] bits, int tipoDeEnlace
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosTransmissoraControleDeErro(int[] bits, int tipoDeEnlace){
    switch (tipoDeEnlace){
      case 0:
        return camadaEnlaceDeDadosTransmissoraControleDeErroBitParidadePar(bits);
      case 1:
        return camadaEnlaceDeDadosTransmissoraControleDeErroBitParidadeImpar(bits); 
      case 2:
        return camadaEnlaceDeDadosTransmissoraControleDeErroCRC(bits);
      case 3:
        return camadaEnlaceDeDadosTransmissoraControleDeErroCodigoDeHamming(bits);
    }
    return null;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosTransmissoraControleDeErroBitParidadePar
  * Funcao: funcao que adiciona o bit de paridade par
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosTransmissoraControleDeErroBitParidadePar(int[] bits){
    int[] arrayFinal = new int[bits.length+1];

    int count = 0;
    for (int i=0; i<bits.length; i++){
      if (bits[i] == 1)
        count++;
    }

    for (int i=0; i<arrayFinal.length; i++){
      if (i<arrayFinal.length-1){
        arrayFinal[i] = bits[i];
      }
      else {
        if (count % 2 == 0)
          arrayFinal[i] = 0;
        else 
          arrayFinal[i] = 1;
      }
    }
    return arrayFinal;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosTransmissoraControleDeErroBitParidadeImpar
  * Funcao: funcao que adiciona o bit de paridade impar
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosTransmissoraControleDeErroBitParidadeImpar(int[] bits){
    int[] arrayFinal = new int[bits.length+1];

    int count = 0;
    for (int i=0; i<bits.length; i++){
      if (bits[i] == 1)
        count++;
    }

    for (int i=0; i<arrayFinal.length; i++){
      if (i<arrayFinal.length-1){
        arrayFinal[i] = bits[i];
      }
      else {
        if (count % 2 == 1)
          arrayFinal[i] = 0;
        else 
          arrayFinal[i] = 1;
      }
    }
    return arrayFinal;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosTransmissoraControleDeErroCRC
  * Funcao: funcao que realiza o codigo de controle CRC
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosTransmissoraControleDeErroCRC(int[] bits){
    int grauPolinomio = 33;
    int[] arrayFinal = new int[bits.length + grauPolinomio -1];
    int[] polinomioDivisor = polinomioDivisor(grauPolinomio);

    for (int i=0; i<bits.length; i++){
        arrayFinal[i] = bits[i];
    }

    int[] arrayAux = new int[grauPolinomio];
    arrayAux = getSubArray(arrayFinal, 0, grauPolinomio);
    int[] arrayAux2 = new int[grauPolinomio-1];

    for (int i=0; i<arrayFinal.length; i++) {
      try {
        arrayAux2 = xor(arrayAux, polinomioDivisor);
        arrayAux = adicionarArrayDentroDeArray(arrayAux2, arrayAux, 0);
        arrayAux[arrayAux.length-1] = arrayFinal[i+grauPolinomio]; //TODO olhar sobre esse numero 5 aqui
      }
      catch (IndexOutOfBoundsException ex){
        break;
      }
    }

    arrayFinal = adicionarArrayDentroDeArray(arrayAux2, arrayFinal, arrayFinal.length-grauPolinomio+1); 

    return arrayFinal;
  }

  /* *********************
  * Metodo: xor
  * Funcao: funcao que realiza operacao xor entre dois arrays
  * Parametros: int[] a, int[] b
  * Retorno: int[]
  ********************* */
  private int[] xor(int[] a, int[] b){
    int[] resultado = new int[a.length];
    int[] arrayVazio = new int[a.length];
    for (int i=0; i<a.length; i++){
      if (isArrayBigger(a, b, 0)){
        if (a[i] != b[i])
          resultado[i] = 1;
        else
          resultado[i] = 0;
      }
      else {
        if (a[i] != arrayVazio[i])
          resultado[i] = 1;
        else
          resultado[i] = 0; 
      }
    }
    
    return getSubArray(resultado, 1, resultado.length);
  }

  /* *********************
  * Metodo: getSubArray
  * Funcao: funcao que gera um array novo a partir de outro maior
  * Parametros: int[] array, int beggining. int ending
  * Retorno: int[]
  ********************* */
  private int[] getSubArray(int[] array, int beggining, int ending){
    int[] arrayFinal = new int[ending-beggining];

    int count = 0;
    for (int i=0; i<array.length; i++){
      if (i>= beggining && i<ending){
        arrayFinal[count] = array[i];
        count++;
      }
    }

    return arrayFinal;
  }

  /* *********************
  * Metodo: polinomioDivisor
  * Funcao: funcao que cria o polimio divisor pedido com tamanho x
  * Parametros: int x
  * Retorno: int[]
  ********************* */
  private int[] polinomioDivisor(int x){
    int[] polinomio = new int[x];
    for (int i=x-1; i>=0; i--){
        if (i == 0 || i == 1 || i == 2 || i == 4 || i == 5 || i == 7 || i == 8 || i == 10 || i == 11 || i == 12 || i == 16 
        || i == 22 || i == 23 || i == 26 || i == 32){
            polinomio[i] = 1;
        }
        else {
            polinomio[i] = 0;
        }
    }
    int y;
    for (int i=0; i<polinomio.length; i++){
        y = polinomio[i];
        polinomio[i] = polinomio[polinomio.length-1-i];
        polinomio[polinomio.length-1-i] = y;
        
        if (i == polinomio.length/2)
            break;
    }
    return polinomio;
  }

  /* *********************
  * Metodo: isArrayBigger
  * Funcao: funcao que serve para detectar se um array pode ser divisivel por outro
  * Parametros: int[] a, int[] b, int n
  * Retorno: boolean
  ********************* */
  private boolean isArrayBigger(int[] a, int[] b, int n){
    if (n == a.length)
      return false;
    if (b[n] > a[n])
      return false;
    else if (a[n] > b[n])
      return true;
    if (a[n] == b[n] && n == 0){
      return isArrayBigger(a, b, (n+1));
    }
    else 
      return true;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosTransmissoraControleDeErroCodigoDeHamming
  * Funcao: funcao que realiza o codigo de hamming para um array de bits
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosTransmissoraControleDeErroCodigoDeHamming(int[] bits){
    int m = bits.length;
    int r = 1;
    while (Math.pow(2, r) < (m + r + 1)){
      r++;
    }


    int[] output = new int[bits.length + r];
    int count = 0;
    for (int i=0; i<output.length; i++){
      if ((i+1) == Math.pow(2, count)){
        count++;
        continue;
      }
      output[i] = bits[i-count];
    }  

    count = 0;
    for (int i=0; i<output.length; i++){
      if((i+1) == Math.pow(2, count)){
        output[i] = calcularParidade(count, output); //para devolver o bit calculado
        count++;
      }
    }
    return output;
  }

  /* *********************
  * Metodo: calcularParidade
  * Funcao: funcao que calcula paridade de um seguimento de posicao dentro de um array
  * Parametros: int num, int[] array
  * Retorno: int
  ********************* */
  private int calcularParidade(int num, int[] array){
    LinkedList<Integer> valoresParaOXor = new LinkedList<>();

    int count = 0;
    for (int i=0; i<array.length; i++){
      if (i+1 > Math.pow(2,count))
        count++;
      if (i+1 != Math.pow(2,count)){
        valoresParaOXor.add(getPosicao(i+1, num));
      }
    }

    //para criar o array que vai armazenas os valores de valoresParaOXor nas posicoes do array original
    count = 0;
    for (int i=0; i<valoresParaOXor.size(); i++){
      if (valoresParaOXor.get(i) == 1){
        count++;
      }
    }

    int[] arrayDeNumeros = new int[count];

    count = 0;
    for (int i=0; i<valoresParaOXor.size(); i++){
      if (valoresParaOXor.get(i) == 1){
        arrayDeNumeros[count] = i;
        count++;
      }
    }

    count = 0;
    int count2 = 0;
    int count3 = 0;
    int pot = 0;

    int[] output = new int[arrayDeNumeros.length];

    for (int i=0; i<array.length; i++){
      if (i+1 == Math.pow(2,pot)){
        pot++;
        count3++;
      }
      else{
        if (count >= arrayDeNumeros.length)
          break;
        if (i-count3 == arrayDeNumeros[count]){
          output[count2] = array[i];
          count++;
          count2++;
        }
      }
    }

    return xorResult(output);
  }

  /* *********************
  * Metodo: getPosicao
  * Funcao: funcao que dado um numero e uma posicao, detecta qual o numero em binario da determinada posicao do array
  * Parametros: int numDado, int bitParidade
  * Retorno: int
  ********************* */
  private int getPosicao(int numDado, int bitParidade){
    int[] bin = decToBin(numDado);
    try {
      if (bin[bitParidade] == 1)
        return 1;
      else 
        return 0;
    }
    catch (IndexOutOfBoundsException ex){
      return 0;
    }
  }

  /* *********************
  * Metodo: decToBin
  * Funcao: funcao que transforma um numero em sua versao em binario dentro de um array
  * Parametros: int num
  * Retorno: int[]
  ********************* */
  private int[] decToBin(int num){
    int multi = 0;
    while (Math.pow(2, multi) < num){
      multi++;
    }
    
    if (Math.pow(2, multi) > num)
      multi--;

    int[] output = new int[multi+1];
    
    if (num == 0){
      output = new int[1];
      return output;
    }
    
    for (int i=output.length-1; i>=0; i--){
      if (num >= Math.pow(2, multi)){
        num -= Math.pow(2, multi);
        multi--;
        output[i] = 1;
      }
      else {
        multi--;
        output[i] = 0;
      }
    }
    return output;
  }

  /* *********************
  * Metodo: xorResult
  * Funcao: funcao que verifica paridade de um array com xor e retorna o valor para manter paridade
  * Parametros: int[] a
  * Retorno: int
  ********************* */
  private int xorResult(int[] a){
    int output = a[0];
    for (int i=1; i<a.length; i++){
      if (output == a[i])
        output = 0;
      else
        output = 1;
    }
    return output;
  }


  //---------------------------------------------------------------------------------------------------------------//
  //---------------------------------------------------------------------------------------------------------------//
  //---------------------------------------------------------------------------------------------------------------//
  //---------------------------------------------------------------------------------------------------------------//
  //---------------------------------------------------------------------------------------------------------------//
  //---------------------------------------------------------------------------------------------------------------//
  //---------------------------------------------------------------------------------------------------------------//
  //---------------------------------------------------------------------------------------------------------------//

  /* *********************
  * Metodo: camadaEnlaceDeDadosReceptora
  * Funcao: funcao geral do receptor para a camada de enlace
  * Parametros: int[] bits, int tipoDeEnlace
  * Retorno: void
  ********************* */
  public void camadaEnlaceDeDadosReceptora(int[] bits, int tipoCodificacao, int tipoDeEnlace, int tipoDeEnlace2){
    if (verificarSeEhAck(bits) != null){ //nao eh um ack
      boolean flag = camadaEnlaceDeDadosReceptoraControleDeErro(bits, tipoDeEnlace2); 
      if (flag){ //array sem erros
        this.meioFisico.codificacaoEspecifica(criarAck(bits), tipoCodificacao, tipoDeEnlace, tipoDeEnlace2, false, taxaDeErro);
        int[] arrayFinal;
        LinkedList<int[]> listaAux = new LinkedList<>();
        int tam = 0;
        for (int i=0; i<bitsArmazenados.size(); i++){
          listaAux.add(i, camadaEnlaceDeDadosReceptoraEnquadramento(bitsArmazenados.get(i), tipoDeEnlace));
          tam += listaAux.get(i).length;
        }
        bitsArmazenados.clear();
        arrayFinal = new int[tam];
        int count = 0;
        for (int i=0; i<listaAux.size(); i++){
          for (int j=0; j<listaAux.get(i).length; j++){
            arrayFinal[count] = listaAux.get(i)[j];
            count++;
          }
        }
        meioAplicacao.decodificarParaString(arrayFinal);
      }      
    }
    else { //eh um ack
      int[] arrayDoAck = getSubArray(bits, 0, 8);
      for (int i=0; i<buffer.getQuadros().size(); i++){
        if (isArrayEqual(arrayDoAck, buffer.getQuadros().get(i).getBits())){
          buffer.getQuadros().get(i).setFlag(true);
          buffer.getQuadros().remove(i);
        }
      }
    }
    
    
  }

  /* *********************
  * Metodo: verificarSeEhAck
  * Funcao: funcao que verifica se um quadro eh ack
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] verificarSeEhAck(int[] bits){
    int[] ack = {1,1,1,1,1,1,1,1};
    if (isArrayEqual(getSubArray(bits, 0, 8), ack))
      return null;
    else 
      return getSubArray(bits, 8, bits.length);
  }

  /* *********************
  * Metodo: criarAck
  * Funcao: funcao que cria um quadro de ack
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] criarAck(int[] bits){
    int[] output = new int[bits.length + 8];
    int count = 0;
    for (int i=0; i<output.length; i++){
      if (i < 8){
        output[i] = 1;
      }
      else {
        output[i] = bits[count];
        count++;
      }
    }
    return output;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosReceptoraEnquadramento
  * Funcao: funcao que chama as funcoes de desenquadramento
  * Parametros: int[] bits, int tipoDeEnlace
  * Retorno: void
  ********************* */
  private int[] camadaEnlaceDeDadosReceptoraEnquadramento(int[] bits, int tipoDeEnlace){
    switch (tipoDeEnlace){
      case 0:
        return (camadaEnlaceDeDadosReceptoraEnquadramentoContagemDeCaracteres(bits));
      case 1:
         return (camadaEnlaceDeDadosReceptoraEnquadramentoInsercaoDeBytes(bits));
      case 2:
        return (camadaEnlaceDeDadosReceptoraEnquadramentoInsercaoDeBits(bits));
      case 3:
        return (camadaEnlaceDeDadosReceptoraEnquadramentoViolacaoDaCamadaFisica(bits));
    }
    return null;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosReceptoraEnquadramentoContagemDeCaracteres
  * Funcao: funcao que desenquadra baseado em contagem de caracteres
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosReceptoraEnquadramentoContagemDeCaracteres(int[] bits) {
    return getSubArrray(bits, 8, bits.length);
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosReceptoraEnquadramentoInsercaoDeBytes
  * Funcao: funcao que desenquadra baseado em insercao de bytes
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosReceptoraEnquadramentoInsercaoDeBytes(int[] bits) {
    int[] esc = {1,1,1,0,0,1,1,1};  //231 - em ascii
    int[] flag = {1,1,1,0,1,0,0,0};  //232 - em ascii

    int count = 0;
    for (int i=8; i<bits.length-8; i++){
      try {
        if (isArrayEqual(getSubArrray(bits, i, i+8), esc) && (isArrayEqual((getSubArrray(bits, i+8, i+16)), esc) 
        || isArrayEqual(getSubArrray(bits, i+8, i+16), flag))){
          count++;
          i+=8;
      }
      }
      catch (IndexOutOfBoundsException ex){
        break;
      }
    }
    
    int[] output = new int[bits.length - 16 - count*8];

    count = 0;
    for (int i=8; i<bits.length-8; i++){
      try {
        if (isArrayEqual(getSubArrray(bits, i, i+8), esc) && (isArrayEqual((getSubArrray(bits, i+8, i+16)), esc) 
        || isArrayEqual(getSubArrray(bits, i+8, i+16), flag))){
          i+=8;
        }
        else {
          output[count] = bits[i];
          count++;
        }
      }
      catch (IndexOutOfBoundsException ex){
        break;
      }

    }
    return output;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosReceptoraEnquadramentoInsercaoDeBits
  * Funcao: funcao que desenquadra baseado em insercao de bits
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosReceptoraEnquadramentoInsercaoDeBits(int[] bits) {
    int count = 0;
    for (int i=8; i<bits.length-8; i++){
      if (EhCindo1EUm0(getSubArrray(bits, i, i+6)))
        count++;
    }

    int[] output = new int[bits.length - 16 - count];

    count = 0;
    for (int i=8; i<bits.length-8; i++){
      if (EhCindo1EUm0(getSubArrray(bits, i, i+6))){
        output[i] = bits[count];
        output[i+1] = bits[count+1];
        output[i+2] = bits[count+2];
        output[i+3] = bits[count+3];
        output[i+4] = bits[count+4];
        count += 5;
        i+=4;
      }
      else{
        output[count] = bits[i];
        count++;
      } 
    }

    return output;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosReceptoraEnquadramentoViolacaoDaCamadaFisica
  * Funcao: funcao que desenquadra baseado em violacao da camada fisica
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosReceptoraEnquadramentoViolacaoDaCamadaFisica(int[] bits){
    //desisti por falta de tempo
    return bits;
  }

  //metodos ajudantes

  /* *********************
  * Metodo: isArrayEqual
  * Funcao: funcao que compara se dois arrays sao iguais
  * Parametros: int[] a, int[] b
  * Retorno: boolean
  ********************* */
  private static boolean isArrayEqual(int[] a, int[] b){
    if (a.length == b.length){
      for (int i=0; i<a.length; i++){
        if (a[i] != b[i]) 
          return false;
      }
    }
    else 
      return false;

    return true;
  }

  /* *********************
  * Metodo: getSubArray
  * Funcao: funcao que cria um novo array com base em posicoes de um array existente
  * Parametros: int[] array, int beggining, int ending
  * Retorno: int[]
  ********************* */
  private static int[] getSubArrray(int[] array, int beggining, int ending){
    int[] arrayFinal = new int[ending-beggining];

    int count = 0;
    for (int i=0; i<array.length; i++){
      if (i>= beggining && i<ending){
        arrayFinal[count] = array[i];
        count++;
      }
    }

    return arrayFinal;
  }

  /* *********************
  * Metodo: isFive1
  * Funcao: funcao que conta se um determinado array tem 5 zeros seguidos
  * Parametros: int[] bits
  * Retorno: boolean
  ********************* */
  private boolean isFive1(int bits[]){
    if (bits[0] == 1 && bits[1] == 1 && bits[2] == 1 && bits[3] == 1 && bits[4] == 1)
      return true;
    return false;
  }

  /* *********************
  * Metodo: ehCinco1EUm0
  * Funcao: funcao que verifica se em um array existe um '0' depois de cinco '1' seguidos
  * Parametros: int[] bits
  * Retorno: boolean
  ********************* */
  private boolean EhCindo1EUm0(int bits[]){
    if (bits[0] == 1 && bits[1] == 1 && bits[2] == 1 && bits[3] == 1 && bits[4] == 1 && bits[5] == 0)
      return true;
    return false;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosReceptoraControleDeErro
  * Funcao: funcao que lida com controle de erro
  * Parametros: int[] bits, int tipoDeEnlace
  * Retorno: boolean
  ********************* */
  private boolean camadaEnlaceDeDadosReceptoraControleDeErro(int[] bits, int tipoDeEnlace){
    int[] output = null;
    switch (tipoDeEnlace){
      case 0:
        output = camadaEnlaceDeDadosReceptoraControleDeErroBitParidadePar(bits);
        break;
      case 1:
        output = camadaEnlaceDeDadosReceptoraControleDeErroBitParidadeImpar(bits);
        break;
      case 2:
        output = camadaEnlaceDeDadosReceptoraControleDeErroCRC(bits);
        break;
      case 3:
        output = camadaEnlaceDeDadosReceptoraControleDeErroCodigoDeHamming(bits);
        break;
    }

    if (output == null)
      return false;
    else{
      bitsArmazenados.add(output);
      return true;
    }
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosReceptoraControleDeErroBitParidadePar
  * Funcao: funcao para detectar erro de paridade par
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosReceptoraControleDeErroBitParidadePar(int[] bits){
    int count = 0;
    for (int i=0; i<bits.length; i++){
      if (bits[i] == 1)
        count++;
    }

    if (count % 2 == 0)
      return getSubArray(bits, 0, bits.length-2);
    else    
      return null;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosReceptoraControleDeErroBitParidadeImpar
  * Funcao: funcao para detectar erro de paidade impar
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosReceptoraControleDeErroBitParidadeImpar(int[] bits){
    int count = 0;
    for (int i=0; i<bits.length; i++){
      if (bits[i] == 1)
        count++;
    }
    if (count % 2 == 1)
      return getSubArray(bits, 0, bits.length-2);
    else    
      return null;
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosReceptoraControleDeErroCRC
  * Funcao: funcao que detecta erro com CRC
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosReceptoraControleDeErroCRC(int[] bits){
    int grauPolinomio = 33;
    int[] arrayAux = new int[grauPolinomio];
    int[] arrayAux2 = new int[grauPolinomio-1];
    int[] polinomioDivisor = polinomioDivisor(grauPolinomio);

    arrayAux = getSubArray(bits, 0, grauPolinomio);

    for (int i=0; i<bits.length; i++){
      try {
        arrayAux2 = xor(arrayAux, polinomioDivisor);
        arrayAux = adicionarArrayDentroDeArray(arrayAux2, arrayAux, 0);
        arrayAux[arrayAux.length-1] = bits[i+grauPolinomio]; 
      }
      catch (IndexOutOfBoundsException ex){
        break;
      }
    }

    boolean flag = false;
    for (int i=0; i<arrayAux2.length; i++){
      if (arrayAux2[i] == 1){
        flag = true;
        break;
      }
    }

    if (flag){
      return null;
    }
    else {
      return getSubArray(bits, 0, bits.length+1-grauPolinomio);
    }
  }

  /* *********************
  * Metodo: camadaEnlaceDeDadosReceptoraControlDeErroCodigoDeHamming
  * Funcao: funcao que detecta erro com o codigo de hamming
  * Parametros: int[] bits
  * Retorno: int[]
  ********************* */
  private int[] camadaEnlaceDeDadosReceptoraControleDeErroCodigoDeHamming(int[] bits){
    int count = 0;
    int[] tempBin = new int[decToBin(bits.length-1).length+1];
    int[] tempBin2 = new int[tempBin.length];
    count = tempBin.length;

    LinkedList<Integer> lista = new LinkedList<>();

    int count2 = 0;
    for (int i=0; i<count; i++){
      for (int j=0; j<bits.length; j++){
        try {
          if (decToBin(j+1)[i] == 1){
            tempBin[count2] = j;
            count2++;
          }
        }
        catch (IndexOutOfBoundsException ex){
          continue;
        }
      }

      count2 = 0;
      for (int j=0; j<bits.length; j++){
        if (tempBin[count2] == j){
          tempBin2[count2] = bits[j];
          count2++;
        }
      }

      lista.add(xorResult(tempBin2));
      count2 = 0;
    }

    boolean flag = false;
    for (int i=0; i<lista.size(); i++){
      if (lista.get(i) == 1)
        flag = true;
      tempBin2[i] = lista.get(i);
    }

    lista.clear();
    count = 0;
    for (int i=0; i<bits.length; i++){
      if ((i+1) == Math.pow(2,count)){
        count++;
        continue;
      }
      lista.add(bits[i]);
    }

    if (flag)
      return null;
    else 
      return transformarLinkedListParaArray(lista);
  }

  /* *********************
  * Metodo: transformarLinkedListParaArray
  * Funcao: funcao que transforma uma LinkedList em Array
  * Parametros: LinkedList<Integer> lista
  * Retorno: int[]
  ********************* */
  private int[] transformarLinkedListParaArray(LinkedList<Integer> lista){
    int[] array = new int[lista.size()];
    for (int i=0; i<lista.size(); i++){
      array[i] = lista.get(i);
    }
    return array;
  }

  //metodos getters e setters
  public MeioFisico getMeioFisico() {
    return meioFisico;
  }

  public void setMeioFisico(MeioFisico meioFisico) {
    this.meioFisico = meioFisico;
  }

  public int getTipoCodificacao() {
    return tipoCodificacao;
  }

  public void setTipoCodificacao(int tipoCodificacao) {
    this.tipoCodificacao = tipoCodificacao;
  }

  public int getTipoDeEnlace() {
    return tipoDeEnlace;
  }

  public void setTipoDeEnlace(int tipoDeEnlace) {
    this.tipoDeEnlace = tipoDeEnlace;
  }

  public int getTipoDeEnlace2() {
    return tipoDeEnlace2;
  }

  public void setTipoDeEnlace2(int tipoDeEnlace2) {
    this.tipoDeEnlace2 = tipoDeEnlace2;
  }

  public Quadro[] getQuadros() {
    return quadros;
  }

  public void setQuadros(Quadro[] quadros) {
    this.quadros = quadros;
  }

  public int getTaxaDeErro() {
    return taxaDeErro;
  }

  public void setTaxaDeErro(int taxaDeErro) {
    this.taxaDeErro = taxaDeErro;
  }  
}