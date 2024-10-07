package com.example.ahorcado

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Variables del juego
    private lateinit var tvCategory: TextView
    private lateinit var tvWord: TextView
    private lateinit var tvLifeCount: TextView
    private lateinit var etLetterInput: EditText
    private lateinit var btnTryAgain: TextView // Nuevo botón para intentar de nuevo

    // Lista de palabras más difíciles y sus pistas
    private val difficultWords = mapOf(
        "HIPOPOTAMO" to "Es un animal acuático y terrestre, muy grande, con un nombre largo.",
        "ORNITORRINCO" to "Mamífero raro con pico de pato y cola de castor, pone huevos.",
        "CANGURO" to "Animal marsupial que se desplaza saltando, vive en Australia.",
        "SERPIENTE" to "Reptil sin patas que se desliza, algunas son venenosas.",
        "ESCARABAJO" to "Insecto con caparazón duro, asociado con el antiguo Egipto.",
        "ARMADILLO" to "Mamífero con una coraza protectora, puede enrollarse en una bola.",
        "CALAMAR" to "Animal marino con tentáculos, algunos son gigantes.",
        "ALBATROS" to "Ave marina, conocida por su gran envergadura de alas."
    )

    private var wordToGuess = ""  // La palabra actual a adivinar
    private var displayedWord = ""  // La palabra mostrada (guiones y letras adivinadas)
    private var remainingLives = 3  // Número de intentos disponibles
    private var guessedLetters = mutableSetOf<Char>() // Letras ya adivinadas

    private var usedWords = mutableSetOf<String>() // Palabras ya adivinadas
    private var level = 1 // Nivel del juego
    private var incorrectGuesses = 0 // Conteo de intentos fallidos

    // Número de letras a revelar al inicio
    private var lettersToReveal = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        tvCategory = findViewById(R.id.tvCategory)
        tvWord = findViewById(R.id.tvWord)
        etLetterInput = findViewById(R.id.etLetterInput)
        tvLifeCount = findViewById(R.id.tvLifeCount)
        btnTryAgain = findViewById(R.id.btnTryAgain)

        // Establecer la primera palabra
        startNewGame()

        // Configurar el teclado para recibir letras
        setupKeyboardInput()

        // Configurar el botón de intentar de nuevo
        btnTryAgain.setOnClickListener {
            startNewGame()
            btnTryAgain.visibility = View.GONE
        }
    }

    // Iniciar un nuevo juego seleccionando una nueva palabra
    private fun startNewGame() {
        // Si todas las palabras fueron usadas, reiniciar
        if (usedWords.size == difficultWords.size) {
            usedWords.clear()
            level = 1 // Reiniciar el nivel
            lettersToReveal = 1 // Reiniciar las letras reveladas
        }

        // Seleccionar una nueva palabra aleatoriamente que no haya sido usada
        val availableWords = difficultWords.keys.filterNot { usedWords.contains(it) }
        val wordAndHint = difficultWords.entries.find { it.key in availableWords } ?: return
        wordToGuess = wordAndHint.key
        val hint = wordAndHint.value
        remainingLives = 3 - (level / 2) // Reducir el número de vidas conforme aumenta el nivel, al menos 1 vida
        incorrectGuesses = 0 // Reiniciar los intentos fallidos
        guessedLetters.clear()

        // Añadir la palabra a las usadas
        usedWords.add(wordToGuess)

        // Revelar algunas letras al principio
        revealInitialLetters()

        // Actualizar las vistas del juego
        tvCategory.text = hint // Mostrar la pista
        tvLifeCount.text = remainingLives.toString()
        initWord() // Inicializar la palabra con guiones y letras reveladas
        etLetterInput.isEnabled = true
    }

    // Revelar algunas letras de la palabra al inicio del juego
    private fun revealInitialLetters() {
        // Ajustar el número de letras a revelar dependiendo de la longitud de la palabra
        val revealCount = (wordToGuess.length / 3).coerceAtLeast(lettersToReveal) // Al menos una letra revelada
        val lettersToRevealList = wordToGuess.toSet().shuffled().take(revealCount) // Tomar letras aleatorias de la palabra
        guessedLetters.addAll(lettersToRevealList)
    }

    // Inicializar la palabra con guiones y letras reveladas
    private fun initWord() {
        displayedWord = wordToGuess.map {
            if (it.uppercaseChar() in guessedLetters) it else '_'
        }.joinToString(" ")
        tvWord.text = displayedWord
    }

    // Configurar entrada desde el teclado
    private fun setupKeyboardInput() {
        etLetterInput.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                val letter = v.text.toString().uppercase().trim()
                if (letter.isNotEmpty() && letter.length == 1) {
                    processLetter(letter[0])
                }
                v.text = null // Limpiar el EditText
            }
            true
        }
    }

    // Procesar la letra ingresada
    private fun processLetter(letter: Char) {
        // Comprobar si la letra ya fue adivinada
        if (guessedLetters.contains(letter)) {
            return
        }

        // Añadir la letra a las adivinadas
        guessedLetters.add(letter)

        // Comprobar si la letra está en la palabra
        if (wordToGuess.contains(letter, ignoreCase = true)) {
            updateDisplayedWord()
        } else {
            // Reducir el número de vidas si la letra no está en la palabra
            remainingLives--
            tvLifeCount.text = remainingLives.toString()
            incorrectGuesses++
            // Dar una pista después de 2 intentos fallidos
            if (incorrectGuesses == 2) {
                revealHintLetter()
            }
        }

        // Comprobar si ha ganado o perdido
        checkGameOver()
    }

    // Dar una pista revelando una letra después de varios intentos fallidos
    private fun revealHintLetter() {
        val unrevealedLetters = wordToGuess.filter { it !in guessedLetters }
        if (unrevealedLetters.isNotEmpty()) {
            val hintLetter = unrevealedLetters.random()
            guessedLetters.add(hintLetter.uppercaseChar())
            updateDisplayedWord()
        }
    }

    // Actualizar la palabra mostrada con las letras adivinadas
    private fun updateDisplayedWord() {
        displayedWord = wordToGuess.map {
            if (it.uppercaseChar() in guessedLetters) it else '_'
        }.joinToString(" ")
        tvWord.text = displayedWord
    }

    // Comprobar si el juego ha terminado
    private fun checkGameOver() {
        if (!displayedWord.contains('_')) {
            // Ha ganado el juego, subir de nivel
            tvWord.text = "¡Ganaste! La palabra era $wordToGuess"
            etLetterInput.isEnabled = false
            level++ // Aumentar el nivel
            lettersToReveal = (lettersToReveal - 1).coerceAtLeast(0) // Reducir las letras reveladas
            // Iniciar un nuevo juego después de un retraso
            etLetterInput.postDelayed({ startNewGame() }, 2000)
        } else if (remainingLives == 0) {
            // Mostrar mensaje de juego perdido y mostrar el botón de intentar de nuevo
            tvWord.text = "¡Perdiste! La palabra era $wordToGuess"
            etLetterInput.isEnabled = false
            btnTryAgain.visibility = View.VISIBLE
        }
    }
}