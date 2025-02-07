# DeepSearch Experimental

An AI-powered research assistant that performs comprehensive web searches across multiple search engines and analyzes results using LLMs.

## Features

- Multi-engine search across Google, Bing, and Yahoo
- Intelligent query generation using OpenAI's GPT models
- Content summarization with local Ollama models
- In-depth analysis using Google's Gemini AI
- Concurrent web scraping with retry mechanisms
- Structured output in Markdown format

## How It Works?  

- **Generates** nine sub-queries using OpenAI's ChatGPT.  
- **Distributes** them across search engines: three queries are searched on Google, three on Bing, and three on Yahoo.  
- **Aggregates** all retrieved content and **processes** it with Google Gemini to generate a comprehensive research report.  

Example on the following query : [How to use search and AST to improve RAG for large codebases?](https://github.com/stephanj/DeepSearch/blob/master/examples/howtousesearchandasttoimproveragforlargecodebases-20250205_215207.md)

### Missing Features 

- [ ] Support YouTube transcripts
- [ ] Support online PDF documents
- [ ] Support scraping of Reddit pages

## Prerequisites

- Java 17+
- Chrome WebDriver
- Ollama (for local summarization, optional)
- API keys:
  - OpenAI 
  - Google (Gemini)

## Environment Variables

Create a `.env` file with:

```
OPENAI_API_KEY=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
OPENAI_MODEL_NAME=gpt-3.5-turbo
GOOGLE_API_KEY=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
GEMINI_MODEL_NAME=gemini-2.0-pro-exp-02-05
OUTPUT_DIRECTORY=search_results

GENERATE_NEW_QUERIES=false
GENERATE_SUMMARIES=false
```

## Installation

1. Clone the repository
2. Install dependencies with Maven
3. Install Ollama and the llama3.1 model
4. Set up environment variables

## Usage

Run the main class:

```bash
java com.devoxx.agentic.Main
```

Enter your research query when prompted. The program will:

1. Generate optimized sub-queries
2. Search across multiple engines
3. Scrape and analyze content
4. Generate summaries (if enabled)
5. Create a comprehensive report

Results are saved in Markdown format in the specified output directory.

## Architecture

- `llm/` - LLM client implementations (OpenAI, Gemini, Ollama)
- `web/` - Web scraping and search functionality
- `search/` - Search engine specific implementations
- `model/` - Data models and content storage
- `util/` - Utility classes for retry logic and result writing

## Contributing

Contributions welcome! Please read our contributing guidelines and submit pull requests.

## License

This project is licensed under the MIT License.
