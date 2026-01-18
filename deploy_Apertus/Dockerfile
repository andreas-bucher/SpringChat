FROM vllm/vllm-openai:latest

# Upgrade transformers (and hf hub) to a version that knows "apertus"
RUN pip install --no-cache-dir "transformers==4.56.0" "huggingface_hub"

ENV HF_HOME=/tmp/hf


CMD ["--host","0.0.0.0","--port","8080","--model","/models/models/Apertus-8B-Instruct-2509","--dtype","auto","--gpu-memory-utilization","0.90","--max-model-len","4096","--trust-remote-code"]
