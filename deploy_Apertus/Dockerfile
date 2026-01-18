FROM vllm/vllm-openai:latest

# Upgrade transformers (and hf hub) to a version that knows "apertus"
RUN pip install --no-cache-dir -U "transformers" "huggingface_hub"

ENV HF_HOME=/root/.cache/huggingface
ENV TRANSFORMERS_CACHE=/root/.cache/huggingface


CMD ["--host","0.0.0.0","--port","8080","--model","swiss-ai/Apertus-8B-Instruct-2509","--dtype","auto","--gpu-memory-utilization","0.90","--max-model-len","4096","--trust-remote-code"]
