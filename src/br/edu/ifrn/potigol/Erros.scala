package br.edu.ifrn.potigol

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import br.edu.ifrn.potigol.parser.potigolLexer
import br.edu.ifrn.potigol.parser.potigolParser
import scala.util.matching.Regex
import scala.util.Try

object Erros extends App {
  case class Erro(erro: String, codigo: String)
  val erros = List(
    Erro("Valor não declarado",
      "escreva x"),
    Erro("Função não declarada",
      "escreva soma(2,3)"),
    Erro("Parâmetro ausente",
      """soma(a, b: Inteiro) = a + b
         soma(2)
      """),
    Erro("Parâmetro a mais 2",
      """soma(a, b: Inteiro) = a + b
         soma(1,2,3)
      """),
    Erro("Parâmetro a mais 1",
      """soma(a: Inteiro) = a + 2
         soma(1,2,3)
      """),
    Erro("Parâmetro a mais 0",
      """soma() = 1 + 2
         soma(1,2,3)
      """),
    Erro("Tipo não definido",
      """soma(a, b: Intei) = a + b
         soma("2","3")
      """),
    Erro("Tipo encontrado diferente do esperado",
      """soma(a, b: Inteiro) = a + b
         soma("2","3")
      """),
    Erro("Tipo encontrado diferente do esperado",
      """soma(a: Int) = a 
         soma("1")
      """),

    Erro("Tipo encontrado diferente do esperado",
      """tipo Pessoa nome: Texto fim
         soma(a: Pessoa) = a 
         soma("1")
      """),
    Erro("Lista sem tipo",
      """soma(lista: Lista) = lista.injete(0)(_+_)
      """),
    Erro("Lista sem tipo",
      """soma(lista: Matriz) = lista
      """),
    Erro("Atribuindo um valor de tipo diferente do esperado",
      """var a = 10
         a := "ok"
      """),
    Erro("Declarando um valor com mesmo nome de variável existente",
      """var a = 10
         a = "ok"
      """),
    Erro("Alterando valor constante",
      """a = 10
         a := "ok"
      """),
    Erro("Declarando novamente um valor",
      """a = 10
         a = "ok"
      """),
    Erro("Declarando novamente um valor",
      """soma() = 10
         soma = 20
      """),
    Erro("Declarando novamente um valor",
      """soma() = 10
         soma := 20
      """),
    Erro("Declarando novamente um valor",
      """soma() = 10
         soma() = 20
      """),
    Erro("Função Recursiva sem tipo",
      """soma(a: Inteiro) = se a>1 então 1 + soma(a-1) senão 0 fim
         soma(3)
      """),
    Erro("",
      """var a = 3
         a[0] := 2
      """),
    Erro("",
      """a = 3
         escreva a[0]
      """),
    Erro("",
      """var a = [1,2,3,4,5]
         a[0] := 2
      """),
    Erro("",
      """a = 4
        a.inverta
      """),
    Erro("",
      """tipo P nome: String fim
        P("ok").inverta
      """),
    Erro("",
      """a=""
        a.maior
      """),
    Erro("",
      """a=1.2
        a.maior
      """),

    Erro("",
      """a = [1,2,3]
        a.maior
      """),

    Erro("",
      """tipo P nome: String fim
        P("ok",4)
      """),
    Erro("",
      """tipo P nome: String fim
        P(4)
      """),

    Erro("Dois erros",
      """a,b = x,y
      """))

  def traduzir(texto: String) = {
    val pNaoDeclarado = "not found: value (\\S+).*".r
    val pParametroAusente = "not enough arguments for method (\\S+)\\:.+Unspecified value parameter (\\S).*".r
    val pParametroMais = "too many arguments for method (\\S+)\\:(.+)\\Z.*".r
    val pTipoIndefinido = "not found: type (\\S+).*".r
    val pTipoDiferente = "type mismatch.+required: (?:\\S*\\.)?([^| .]+) .*".r
    val pParametroTipo = "(?:type|class) (\\S+) takes type parameters .*".r
    val pVariavelJaExiste = "(\\S+) is already defined as variable .+".r
    val pAlterarValorConstante = "reassignment to val \\| (\\S+).*".r
    val pValorJaDeclarado = "(\\S+) is already defined as value.*".r
    val pFuncaoJaDefinida = "method (\\S+) is defined twice .*".r
    val pFuncaoRecursivaSemTipo = "recursive method (\\S+) needs result type.*".r
    val pMatrizNaoDeclarada = "value (?:get|update) is not (\\S+) member of.*".r
    val pMetodoNaoExiste = "value (\\S+) is not a member of (?:\\S*\\.)?(\\S+) .*".r

    def mensagens(s: String) = s.replace("\n", " | ") match {
      case pNaoDeclarado(a) => s"Valor '${a}' não declarado."
      case pParametroAusente(a, b) => s"A função '${a}' precisa de mais parâmetros.\nVocê esqueceu de fornecer o parâmetro '${b}'."
      case pParametroMais("apply", b) => s"Você forneceu mais parâmetros do que o necessário.\nColoque apenas ${b.count(':' == _)} parâmetro(s)."
      case pParametroMais(a, b) if b.count(':' == _) == 0 => s"A função '${a}' não precisa de parâmetro."
      case pParametroMais(a, b) if b.count(':' == _) == 1 => s"A função '${a}' precisa de apenas 1 parâmetro."
      case pParametroMais(a, b) => s"A função '${a}' precisa de apenas ${b.count(':' == _)} parâmetros."
      case pTipoIndefinido(a) => s"O tipo '${a}' não existe.\nNão seria 'Inteiro', 'Real' ou 'Texto'?"
      case pTipoDiferente("Int") => s"Tipo errado.\nEu estava esperando um valor do tipo 'Inteiro'."
      case pTipoDiferente("Double") => s"Tipo errado.\nEu estava esperando um valor do tipo 'Real'."
      case pTipoDiferente("String") => s"Tipo errado.\nEu estava esperando um valor do tipo 'Texto'."
      case pTipoDiferente(a) => s"Tipo errado.\nEu estava esperando um valor do tipo '${a}'."
      case pParametroTipo(a) => s"'${a}' precisa do tipo.\nPoderia ser '${a}[Inteiro]' ou '${a}[Texto]'?"
      case pVariavelJaExiste(a) => s"A variável '${a}' já existe.\nSe quiser modificar o valor de '${a}' use ':=' ao invés de '='."
      case pAlterarValorConstante(a) => s"'${a}' é um valor constante, não pode ser alterado."
      case pValorJaDeclarado(a) => s"O valor '${a}' já foi declarado antes.\nUse outro nome."
      case pFuncaoJaDefinida(a) => s"Já existe uma função chamada '${a}'.\nUse outro nome."
      case pFuncaoRecursivaSemTipo(a) => s"A função recursiva '${a}' precisa definir o tipo do valor de retorno."
      case pMatrizNaoDeclarada(a) => s"A variável '${a}' não é uma Lista mutável."
      case pMetodoNaoExiste(a, "Int") => s"Valores do tipo 'Inteiro' não possuem o método '${a}'."
      case pMetodoNaoExiste(a, "Double") => s"Valores do tipo 'Real' não possuem o método '${a}'."
      case pMetodoNaoExiste(a, "String") => s"Valores do tipo 'Texto' não possuem o método '${a}'."
      case pMetodoNaoExiste(a, b) => s"Valores do tipo '${b}' não possuem o método '${a}'."
      case a => a
    }
    val inicio = texto.indexOf("error:")
    val s = texto.drop(inicio).split('^')(0).split(": ", 3).drop(1)(1)
    mensagens(s)
  }
  def texto(erro: String) = {
    traduzir(erro)
  }

  def imprimir_erro(erro: Erro) = {
    val code = erro.codigo

    val input = new ANTLRInputStream(code)
    val lexer = new potigolLexer(input)
    val tokens = new CommonTokenStream(lexer)
    val parser = new potigolParser(tokens)
    val tree = parser.prog()
    val walker = new ParseTreeWalker()
    val listener = new Listener()
    walker.walk(listener, tree)
    val c = new Compilador(false, false)
    println("=======================")
    println(erro.erro)
    println("-----------------------")
    println(erro.codigo)
    println("-----------------------")
    println(texto(c.avaliar(listener.getSaida).toString()))
  }

 // erros.drop(24).foreach { imprimir_erro }
}